package net.lab1024.sa.admin.module.business.pay.service;

import net.lab1024.sa.admin.module.business.pay.constant.PayReconBizTypeEnum;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 解析支付宝/微信交易账单 CSV（含 zip）。
 */
@Component
public class PayBillParser {

    public List<PayBillLine> parse(byte[] data, String filename) {
        if (data == null || data.length == 0) {
            throw new BusinessException("账单文件为空");
        }
        String name = StringUtils.defaultString(filename).toLowerCase(Locale.ROOT);
        if (isGzip(data) || name.endsWith(".gz")) {
            data = ungzip(data);
            name = name.replace(".gz", ".csv");
        }
        if (name.endsWith(".zip") || isZip(data)) {
            data = unzipFirstText(data);
            name = "bill.csv";
        }
        String text = decode(data, name);
        return parseText(text);
    }

    public List<PayBillLine> parseText(String text) {
        if (StringUtils.isBlank(text)) {
            throw new BusinessException("账单内容为空");
        }
        String[] rawLines = text.split("\\r?\\n");
        int headerIndex = -1;
        String[] headerCols = null;
        for (int i = 0; i < rawLines.length; i++) {
            String line = stripBom(rawLines[i]).trim();
            if (line.startsWith("#")) {
                line = line.substring(1);
            }
            if (line.contains("商户订单号") && (line.contains("支付宝交易号") || line.contains("微信订单号") || line.contains("交易号"))) {
                headerIndex = i;
                headerCols = splitCsv(line);
                break;
            }
        }
        if (headerIndex < 0 || headerCols == null) {
            throw new BusinessException("无法识别账单表头，请上传支付宝或微信交易账单 CSV");
        }
        Map<String, Integer> idx = indexOf(headerCols);
        List<PayBillLine> lines = new ArrayList<>();
        for (int i = headerIndex + 1; i < rawLines.length; i++) {
            String line = stripBom(rawLines[i]).trim();
            if (StringUtils.isBlank(line) || line.startsWith("#") || line.startsWith("合计") || line.contains("总交易净额")) {
                continue;
            }
            String[] cols = splitCsv(line);
            if (cols.length < 3) {
                continue;
            }
            PayBillLine bill = toLine(idx, cols);
            if (bill != null && StringUtils.isNotBlank(bill.getOrderNo())) {
                lines.add(bill);
            }
        }
        return lines;
    }

    private PayBillLine toLine(Map<String, Integer> idx, String[] cols) {
        String orderNo = cell(idx, cols, "商户订单号");
        if (StringUtils.isBlank(orderNo) || "商户订单号".equals(orderNo)) {
            return null;
        }
        String biz = first(idx, cols, "业务类型", "交易状态", "交易类型");
        String refundNo = first(idx, cols, "退款批次号/请求号", "商户退款单号", "退款单号");
        boolean refund = isRefund(biz, refundNo, cell(idx, cols, "微信退款单号"));
        String amountText = refund
                ? first(idx, cols, "退款金额", "商家实收（元）", "订单金额（元）", "订单金额", "应结订单金额")
                : first(idx, cols, "订单金额（元）", "商家实收（元）", "订单金额", "应结订单金额");
        Integer fen = yuanToFen(amountText);
        if (fen == null) {
            return null;
        }
        fen = Math.abs(fen);
        PayBillLine line = new PayBillLine();
        line.setBizType(refund ? PayReconBizTypeEnum.REFUND.getValue() : PayReconBizTypeEnum.TRADE.getValue());
        line.setOrderNo(orderNo.trim());
        line.setTradeNo(first(idx, cols, "支付宝交易号", "微信订单号", "交易号"));
        line.setRefundNo(refundNo);
        line.setAmountFen(fen);
        line.setChannelStatus(StringUtils.defaultIfBlank(biz, refund ? "退款" : "交易"));
        line.setChannelTime(first(idx, cols, "完成时间", "交易时间", "创建时间"));
        return line;
    }

    private boolean isRefund(String biz, String refundNo, String wxRefundNo) {
        String text = StringUtils.defaultString(biz);
        if (text.contains("退") || "REFUND".equalsIgnoreCase(text)) {
            return true;
        }
        return hasToken(refundNo) || hasToken(wxRefundNo);
    }

    private boolean hasToken(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String text = value.trim();
        return !"-".equals(text) && !"/".equals(text) && !"0".equals(text) && !"0.00".equals(text);
    }

    private Map<String, Integer> indexOf(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String key = header[i] == null ? "" : header[i].trim();
            if (key.startsWith("\uFEFF")) {
                key = key.substring(1);
            }
            map.put(key, i);
        }
        return map;
    }

    private String first(Map<String, Integer> idx, String[] cols, String... names) {
        for (String name : names) {
            String value = cell(idx, cols, name);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String cell(Map<String, Integer> idx, String[] cols, String name) {
        Integer i = idx.get(name);
        if (i == null || i < 0 || i >= cols.length) {
            return null;
        }
        String value = cols[i];
        return value == null ? null : value.trim();
    }

    static Integer yuanToFen(String yuan) {
        if (StringUtils.isBlank(yuan) || "-".equals(yuan) || "/".equals(yuan)) {
            return null;
        }
        String text = yuan.replace("`", "").replace(",", "").replace("元", "").trim();
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text).multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private String[] splitCsv(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quote = !quote;
                continue;
            }
            if (c == ',' && !quote) {
                list.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        list.add(cur.toString());
        return list.toArray(new String[0]);
    }

    private String decode(byte[] data, String filename) {
        boolean preferGbk = filename != null && (filename.contains("alipay") || filename.contains("ali")
                || filename.contains("支付宝") || filename.contains("2088"));
        Charset first = preferGbk ? Charset.forName("GBK") : StandardCharsets.UTF_8;
        Charset second = preferGbk ? StandardCharsets.UTF_8 : Charset.forName("GBK");
        String a = new String(data, first);
        if (a.contains("商户订单号") || a.contains("支付宝") || a.contains("微信")) {
            return a;
        }
        String b = new String(data, second);
        return b.contains("商户订单号") ? b : a;
    }

    private byte[] unzipFirstText(byte[] zipBytes) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = StringUtils.defaultString(entry.getName()).toLowerCase(Locale.ROOT);
                if (name.endsWith(".csv") || name.endsWith(".txt") || name.contains("明细")) {
                    return readAll(zip);
                }
            }
        } catch (Exception e) {
            throw new BusinessException("解压账单失败：" + e.getMessage());
        }
        throw new BusinessException("压缩包内没有 CSV/TXT 账单");
    }

    private byte[] readAll(ZipInputStream zip) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        zip.transferTo(out);
        return out.toByteArray();
    }

    private boolean isZip(byte[] data) {
        return data.length > 3 && data[0] == 'P' && data[1] == 'K';
    }

    private boolean isGzip(byte[] data) {
        return data.length > 2 && (data[0] & 0xff) == 0x1f && (data[1] & 0xff) == 0x8b;
    }

    private byte[] ungzip(byte[] data) {
        try (java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            gzip.transferTo(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("解压gzip账单失败：" + e.getMessage());
        }
    }

    private String stripBom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }
}
