package net.lab1024.sa.admin.module.business.pay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconBatchStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconBizTypeEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconMatchStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconSourceEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.admin.module.business.pay.dao.PayOrderDao;
import net.lab1024.sa.admin.module.business.pay.dao.PayReconBatchDao;
import net.lab1024.sa.admin.module.business.pay.dao.PayReconItemDao;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayOrderEntity;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayReconBatchEntity;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayReconItemEntity;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconBatchQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconHandleForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconItemQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconPullForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconBatchVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconItemExcelVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconItemVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartEnumUtil;
import net.lab1024.sa.base.common.util.SmartExcelUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 支付对账：渠道账单与本地 t_pay_order 按商户订单号匹配。
 */
@Slf4j
@Service
public class PayReconService {

    @Resource
    private PayOrderDao payOrderDao;

    @Resource
    private PayReconBatchDao payReconBatchDao;

    @Resource
    private PayReconItemDao payReconItemDao;

    @Resource
    private PayBillParser payBillParser;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private WeChatPayClient weChatPayClient;

    @Resource
    private PlatformTransactionManager transactionManager;

    public ResponseDTO<PageResult<PayReconBatchVO>> queryBatch(PayReconBatchQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PayReconBatchVO> list = payReconBatchDao.query(page, queryForm);
        list.forEach(this::fillBatchYuan);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<PayReconBatchVO> detail(Long batchId) {
        PayReconBatchEntity entity = payReconBatchDao.selectById(batchId);
        if (entity == null) {
            return ResponseDTO.userErrorParam("对账批次不存在");
        }
        PayReconBatchVO vo = SmartBeanUtil.copy(entity, PayReconBatchVO.class);
        fillBatchYuan(vo);
        return ResponseDTO.ok(vo);
    }

    public ResponseDTO<PageResult<PayReconItemVO>> queryItem(PayReconItemQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PayReconItemVO> list = payReconItemDao.query(page, queryForm);
        list.forEach(this::fillItemYuan);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<PayReconBatchVO> pull(PayReconPullForm form) {
        validateBillDate(form.getBillDate(), true);
        Integer channel = form.getPayChannel();
        LocalDate date = form.getBillDate();
        try {
            List<PayBillLine> lines;
            String fileName;
            if (PayChannelEnum.ALIPAY.equalsValue(channel)) {
                if (alipayClient.isMock() || !Boolean.TRUE.equals(alipayClient.getProperties().getEnabled())) {
                    throw new BusinessException("支付宝未开启真实收款，请用「演示对账」或上传账单");
                }
                String url = alipayClient.billDownloadUrl(date.toString(), "trade");
                if (StringUtils.isBlank(url)) {
                    throw new BusinessException("支付宝未返回账单地址");
                }
                lines = payBillParser.parse(download(url), "alipay-bill.zip");
                fileName = "alipay-trade.zip";
            } else if (PayChannelEnum.WECHAT.equalsValue(channel)) {
                if (weChatPayClient.isMock() || !weChatPayClient.isConfigured()) {
                    throw new BusinessException("微信支付未配置官方账单，请上传交易账单或使用演示对账");
                }
                lines = payBillParser.parse(weChatPayClient.downloadTradeBill(date.toString()), "wechat-bill.csv.gz");
                fileName = "wechat-trade.csv.gz";
            } else {
                throw new BusinessException("支付渠道错误");
            }
            return ResponseDTO.ok(matchAndSave(date, channel, PayReconSourceEnum.PULL.getValue(), lines, fileName));
        } catch (Exception e) {
            String msg = e.getMessage();
            saveFailBatch(date, channel, PayReconSourceEnum.PULL.getValue(), msg);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("拉取对账失败：" + msg);
        }
    }

    public ResponseDTO<PayReconBatchVO> mock(PayReconPullForm form) {
        validateBillDate(form.getBillDate(), false);
        List<PayBillLine> lines = mockLines(form.getPayChannel(), form.getBillDate());
        return ResponseDTO.ok(matchAndSave(form.getBillDate(), form.getPayChannel(),
                PayReconSourceEnum.MOCK.getValue(), lines, "demo-bill.csv"));
    }

    public ResponseDTO<PayReconBatchVO> upload(Integer payChannel, LocalDate billDate, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseDTO.userErrorParam("请选择账单文件");
        }
        if (payChannel == null || billDate == null) {
            return ResponseDTO.userErrorParam("请选择渠道和账单日期");
        }
        if (!PayChannelEnum.WECHAT.equalsValue(payChannel) && !PayChannelEnum.ALIPAY.equalsValue(payChannel)) {
            return ResponseDTO.userErrorParam("支付渠道错误");
        }
        validateBillDate(billDate, false);
        String original = StringUtils.defaultString(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!(original.endsWith(".csv") || original.endsWith(".txt") || original.endsWith(".zip") || original.endsWith(".gz"))) {
            return ResponseDTO.userErrorParam("仅支持 csv / txt / zip / gz");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseDTO.userErrorParam("账单文件不能超过 10MB");
        }
        try {
            List<PayBillLine> lines = payBillParser.parse(file.getBytes(), file.getOriginalFilename());
            return ResponseDTO.ok(matchAndSave(billDate, payChannel, PayReconSourceEnum.UPLOAD.getValue(),
                    lines, file.getOriginalFilename()));
        } catch (BusinessException e) {
            saveFailBatch(billDate, payChannel, PayReconSourceEnum.UPLOAD.getValue(), e.getMessage());
            throw e;
        } catch (Exception e) {
            saveFailBatch(billDate, payChannel, PayReconSourceEnum.UPLOAD.getValue(), e.getMessage());
            throw new BusinessException("读取账单失败：" + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> handle(PayReconHandleForm form) {
        PayReconItemEntity item = payReconItemDao.selectById(form.getItemId());
        if (item == null) {
            return ResponseDTO.userErrorParam("对账明细不存在");
        }
        if (PayReconMatchStatusEnum.MATCHED.equalsValue(item.getMatchStatus())) {
            return ResponseDTO.userErrorParam("完全匹配的明细无需核销");
        }
        if (Boolean.TRUE.equals(item.getHandledFlag())) {
            return ResponseDTO.userErrorParam("该差异已核销");
        }
        item.setHandledFlag(true);
        item.setRemark(StringUtils.defaultIfBlank(form.getRemark(), "人工核销"));
        item.setUpdateTime(LocalDateTime.now());
        payReconItemDao.updateById(item);
        return ResponseDTO.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteBatch(Long batchId) {
        PayReconBatchEntity batch = payReconBatchDao.selectById(batchId);
        if (batch == null) {
            return ResponseDTO.userErrorParam("对账批次不存在");
        }
        payReconItemDao.delete(new LambdaQueryWrapper<PayReconItemEntity>().eq(PayReconItemEntity::getBatchId, batchId));
        payReconBatchDao.deleteById(batchId);
        return ResponseDTO.ok();
    }

    public void exportItem(Long batchId, HttpServletResponse response) throws IOException {
        PayReconBatchEntity batch = payReconBatchDao.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("对账批次不存在");
        }
        List<PayReconItemVO> list = payReconItemDao.listByBatchId(batchId);
        list.forEach(this::fillItemYuan);
        List<PayReconItemExcelVO> excelList = new ArrayList<>();
        for (PayReconItemVO item : list) {
            PayReconItemExcelVO excel = new PayReconItemExcelVO();
            excel.setMatchStatus(SmartEnumUtil.getEnumDescByValue(item.getMatchStatus(), PayReconMatchStatusEnum.class));
            excel.setBizType(SmartEnumUtil.getEnumDescByValue(item.getBizType(), PayReconBizTypeEnum.class));
            excel.setOrderNo(item.getOrderNo());
            excel.setLocalAmountYuan(item.getLocalAmountYuan());
            excel.setLocalStatus(SmartEnumUtil.getEnumDescByValue(item.getLocalStatus(), PayStatusEnum.class));
            excel.setChannelAmountYuan(item.getChannelAmountYuan());
            excel.setChannelStatus(item.getChannelStatus());
            excel.setChannelTradeNo(item.getChannelTradeNo());
            excel.setDiffAmountYuan(item.getDiffAmountYuan());
            excel.setHandledFlag(Boolean.TRUE.equals(item.getHandledFlag()) ? "是" : "否");
            excel.setRemark(item.getRemark());
            excelList.add(excel);
        }
        SmartExcelUtil.exportExcel(response, "支付对账明细-" + batch.getBillDate() + ".xlsx", "对账明细",
                PayReconItemExcelVO.class, excelList);
    }

    private PayReconBatchVO matchAndSave(LocalDate billDate, Integer payChannel, Integer sourceType,
                                         List<PayBillLine> lines, String fileName) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> doMatchAndSave(billDate, payChannel, sourceType, lines, fileName));
    }

    private PayReconBatchVO doMatchAndSave(LocalDate billDate, Integer payChannel, Integer sourceType,
                                           List<PayBillLine> lines, String fileName) {
        PayReconBatchEntity batch = new PayReconBatchEntity();
        batch.setBillDate(billDate);
        batch.setPayChannel(payChannel);
        batch.setSourceType(sourceType);
        batch.setBatchStatus(PayReconBatchStatusEnum.RUNNING.getValue());
        batch.setFileName(fileName);
        batch.setCreateUserId(AdminRequestUtil.getRequestUserId());
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        zeroCounts(batch);
        payReconBatchDao.insert(batch);

        List<PayOrderEntity> locals = payOrderDao.listForRecon(payChannel, billDate);
        Set<Long> used = new HashSet<>();
        List<PayReconItemEntity> items = new ArrayList<>();
        int channelAmount = 0;
        for (PayBillLine line : lines) {
            channelAmount += defaultInt(line.getAmountFen());
            PayOrderEntity local = findLocal(line);
            PayReconItemEntity item = new PayReconItemEntity();
            item.setBatchId(batch.getBatchId());
            item.setBizType(line.getBizType());
            item.setChannelTradeNo(line.getTradeNo());
            item.setChannelOrderNo(line.getOrderNo());
            item.setChannelAmount(line.getAmountFen());
            item.setChannelStatus(line.getChannelStatus());
            item.setChannelTime(line.getChannelTime());
            item.setOrderNo(line.getOrderNo());
            item.setHandledFlag(false);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            if (local == null) {
                item.setMatchStatus(PayReconMatchStatusEnum.CHANNEL_ONLY.getValue());
                item.setRemark("渠道有、本地无对应支付单");
            } else {
                used.add(local.getPayOrderId());
                fillLocal(item, local, line);
            }
            items.add(item);
        }
        for (PayOrderEntity local : locals) {
            if (used.contains(local.getPayOrderId())) {
                continue;
            }
            PayReconItemEntity item = new PayReconItemEntity();
            item.setBatchId(batch.getBatchId());
            item.setBizType(PayReconBizTypeEnum.TRADE.getValue());
            item.setPayOrderId(local.getPayOrderId());
            item.setOrderNo(local.getOrderNo());
            item.setLocalAmount(local.getAmount());
            item.setLocalStatus(local.getPayStatus());
            item.setMatchStatus(PayReconMatchStatusEnum.LOCAL_ONLY.getValue());
            item.setHandledFlag(false);
            item.setRemark("本地已收款/退款，渠道账单没有");
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            items.add(item);
        }
        for (PayReconItemEntity item : items) {
            payReconItemDao.insert(item);
        }
        int localAmount = items.stream().mapToInt(e -> defaultInt(e.getLocalAmount())).sum();

        batch.setChannelCount(lines.size());
        batch.setLocalCount((int) items.stream().filter(e -> e.getPayOrderId() != null).count());
        batch.setMatchedCount(count(items, PayReconMatchStatusEnum.MATCHED));
        batch.setAmountDiffCount(count(items, PayReconMatchStatusEnum.AMOUNT_DIFF));
        batch.setStatusDiffCount(count(items, PayReconMatchStatusEnum.STATUS_DIFF));
        batch.setLocalOnlyCount(count(items, PayReconMatchStatusEnum.LOCAL_ONLY));
        batch.setChannelOnlyCount(count(items, PayReconMatchStatusEnum.CHANNEL_ONLY));
        batch.setLocalAmount(localAmount);
        batch.setChannelAmount(channelAmount);
        batch.setBatchStatus(PayReconBatchStatusEnum.DONE.getValue());
        batch.setRemark(summary(batch));
        batch.setUpdateTime(LocalDateTime.now());
        payReconBatchDao.updateById(batch);

        PayReconBatchVO vo = SmartBeanUtil.copy(batch, PayReconBatchVO.class);
        fillBatchYuan(vo);
        return vo;
    }

    private void fillLocal(PayReconItemEntity item, PayOrderEntity local, PayBillLine line) {
        item.setPayOrderId(local.getPayOrderId());
        item.setOrderNo(local.getOrderNo());
        boolean refund = PayReconBizTypeEnum.REFUND.equalsValue(line.getBizType());
        int localFen = refund ? defaultInt(local.getRefundAmount()) : defaultInt(local.getAmount());
        item.setLocalAmount(localFen);
        item.setLocalStatus(local.getPayStatus());
        int channelFen = defaultInt(line.getAmountFen());
        item.setDiffAmount(channelFen - localFen);
        boolean amountOk = Objects.equals(localFen, channelFen);
        boolean statusOk = refund ? isRefundStatus(local.getPayStatus()) : isPaidStatus(local.getPayStatus());
        if (amountOk && statusOk) {
            item.setMatchStatus(PayReconMatchStatusEnum.MATCHED.getValue());
            item.setRemark("金额、状态一致");
            return;
        }
        if (!amountOk) {
            item.setMatchStatus(PayReconMatchStatusEnum.AMOUNT_DIFF.getValue());
            item.setRemark("本地" + fenYuan(localFen) + "元，渠道" + fenYuan(channelFen) + "元"
                    + (statusOk ? "" : "；状态也不符"));
            return;
        }
        item.setMatchStatus(PayReconMatchStatusEnum.STATUS_DIFF.getValue());
        item.setRemark("本地状态与渠道" + StringUtils.defaultString(line.getChannelStatus()) + "不一致");
    }

    private PayOrderEntity findLocal(PayBillLine line) {
        if (StringUtils.isNotBlank(line.getRefundNo())) {
            PayOrderEntity byRefund = payOrderDao.selectByRefundNo(line.getRefundNo());
            if (byRefund != null) {
                return byRefund;
            }
        }
        if (StringUtils.isNotBlank(line.getOrderNo())) {
            PayOrderEntity byNo = payOrderDao.selectByOrderNo(line.getOrderNo());
            if (byNo != null) {
                return byNo;
            }
        }
        if (StringUtils.isNotBlank(line.getTradeNo())) {
            return payOrderDao.selectByTransactionId(line.getTradeNo());
        }
        return null;
    }

    private void validateBillDate(LocalDate billDate, boolean officialPull) {
        if (billDate == null) {
            throw new BusinessException("账单日期不能为空");
        }
        LocalDate today = LocalDate.now();
        if (billDate.isAfter(today)) {
            throw new BusinessException("账单日期不能晚于今天");
        }
        if (officialPull && !billDate.isBefore(today)) {
            throw new BusinessException("官方账单为 T+1，请选择昨天及更早的日期");
        }
    }

    private void saveFailBatch(LocalDate billDate, Integer payChannel, Integer sourceType, String errorMsg) {
        try {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.executeWithoutResult(status -> {
                PayReconBatchEntity batch = new PayReconBatchEntity();
                batch.setBillDate(billDate);
                batch.setPayChannel(payChannel);
                batch.setSourceType(sourceType);
                batch.setBatchStatus(PayReconBatchStatusEnum.FAIL.getValue());
                batch.setErrorMsg(StringUtils.left(errorMsg, 500));
                batch.setRemark("对账失败");
                batch.setCreateUserId(AdminRequestUtil.getRequestUserId());
                batch.setCreateTime(LocalDateTime.now());
                batch.setUpdateTime(LocalDateTime.now());
                zeroCounts(batch);
                payReconBatchDao.insert(batch);
            });
        } catch (Exception e) {
            log.warn("记录失败对账批次异常: {}", e.getMessage());
        }
    }

    private List<PayBillLine> mockLines(Integer payChannel, LocalDate billDate) {
        List<PayOrderEntity> locals = payOrderDao.listForRecon(payChannel, billDate);
        List<PayBillLine> lines = new ArrayList<>();
        for (PayOrderEntity local : locals) {
            PayBillLine trade = new PayBillLine();
            trade.setBizType(PayReconBizTypeEnum.TRADE.getValue());
            trade.setOrderNo(local.getOrderNo());
            trade.setTradeNo(StringUtils.defaultIfBlank(local.getTransactionId(), "MOCK" + local.getPayOrderId()));
            trade.setAmountFen(local.getAmount());
            trade.setChannelStatus("SUCCESS");
            trade.setChannelTime(String.valueOf(local.getSuccessTime()));
            lines.add(trade);
            if (defaultInt(local.getRefundAmount()) > 0) {
                PayBillLine refund = new PayBillLine();
                refund.setBizType(PayReconBizTypeEnum.REFUND.getValue());
                refund.setOrderNo(local.getOrderNo());
                refund.setTradeNo(local.getTransactionId());
                refund.setRefundNo(local.getRefundNo());
                refund.setAmountFen(local.getRefundAmount());
                refund.setChannelStatus("REFUND");
                refund.setChannelTime(String.valueOf(local.getRefundTime()));
                lines.add(refund);
            }
        }
        if (weChatPayClient.isMock() || alipayClient.isMock()) {
            log.info("演示对账按本地已支付/已退款单生成账单，渠道={} 日期={} 行数={}", payChannel, billDate, lines.size());
        }
        return lines;
    }

    private byte[] download(String url) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("下载渠道账单失败，HTTP " + response.statusCode());
            }
            return response.body();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("下载渠道账单失败：" + e.getMessage());
        }
    }

    private void zeroCounts(PayReconBatchEntity batch) {
        batch.setLocalCount(0);
        batch.setChannelCount(0);
        batch.setMatchedCount(0);
        batch.setAmountDiffCount(0);
        batch.setStatusDiffCount(0);
        batch.setLocalOnlyCount(0);
        batch.setChannelOnlyCount(0);
        batch.setLocalAmount(0);
        batch.setChannelAmount(0);
    }

    private int count(List<PayReconItemEntity> items, PayReconMatchStatusEnum status) {
        return (int) items.stream().filter(e -> status.equalsValue(e.getMatchStatus())).count();
    }

    private String summary(PayReconBatchEntity batch) {
        return String.format(Locale.ROOT, "匹配%d 金额不符%d 状态不符%d 仅本地%d 仅渠道%d",
                defaultInt(batch.getMatchedCount()), defaultInt(batch.getAmountDiffCount()),
                defaultInt(batch.getStatusDiffCount()), defaultInt(batch.getLocalOnlyCount()),
                defaultInt(batch.getChannelOnlyCount()));
    }

    private boolean isPaidStatus(Integer status) {
        return PayStatusEnum.SUCCESS.equalsValue(status)
                || PayStatusEnum.REFUNDING.equalsValue(status)
                || PayStatusEnum.REFUND.equalsValue(status);
    }

    private boolean isRefundStatus(Integer status) {
        return PayStatusEnum.REFUNDING.equalsValue(status) || PayStatusEnum.REFUND.equalsValue(status);
    }

    private void fillBatchYuan(PayReconBatchVO vo) {
        vo.setLocalAmountYuan(toYuan(vo.getLocalAmount()));
        vo.setChannelAmountYuan(toYuan(vo.getChannelAmount()));
    }

    private void fillItemYuan(PayReconItemVO vo) {
        vo.setLocalAmountYuan(toYuan(vo.getLocalAmount()));
        vo.setChannelAmountYuan(toYuan(vo.getChannelAmount()));
        vo.setDiffAmountYuan(toYuan(vo.getDiffAmount()));
    }

    private BigDecimal toYuan(Integer fen) {
        if (fen == null) {
            return null;
        }
        return BigDecimal.valueOf(fen).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private String fenYuan(int fen) {
        return toYuan(fen).toPlainString();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
