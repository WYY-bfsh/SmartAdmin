package net.lab1024.sa.admin.module.business.pay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.pay.config.AlipayProperties;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

/**
 * 支付宝官方 SDK 封装。配置不完整时不会初始化，调用即报错提示。
 */
@Slf4j
@Component
public class AlipayClient {

    @Resource
    private AlipayProperties properties;

    private volatile com.alipay.api.AlipayClient client;

    public AlipayProperties getProperties() {
        return properties;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getEnabled()) || isMock();
    }

    public boolean isMock() {
        return Boolean.TRUE.equals(properties.getMock());
    }

    public boolean isCertMode() {
        return properties.isCertMode();
    }

    public boolean isConfigured() {
        if (isMock()) {
            return true;
        }
        if (StringUtils.isBlank(properties.getAppId()) || StringUtils.isBlank(properties.getNotifyUrl())) {
            return false;
        }
        if (!hasPrivateKey()) {
            return false;
        }
        if (isCertMode()) {
            return StringUtils.isNoneBlank(properties.getAppCertPath(),
                    properties.getAlipayPublicCertPath(), properties.getAlipayRootCertPath());
        }
        return StringUtils.isNotBlank(properties.getAlipayPublicKey());
    }

    public boolean hasPrivateKey() {
        return StringUtils.isNotBlank(properties.getPrivateKey())
                || StringUtils.isNotBlank(properties.getPrivateKeyPath());
    }

    /**
     * 当面付：预下单，返回二维码内容
     */
    public String precreate(String orderNo, String subject, String totalAmount, String body) {
        if (isMock()) {
            return "https://qr.alipay.com/mock/" + orderNo;
        }
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(orderNo);
        model.setSubject(subject);
        model.setTotalAmount(totalAmount);
        model.setBody(body);
        model.setProductCode("FACE_TO_FACE_PAYMENT");
        model.setTimeoutExpress(properties.getTimeoutExpress());
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(properties.getNotifyUrl());
        AlipayTradePrecreateResponse response = (AlipayTradePrecreateResponse) call(request, false);
        if (response == null || StringUtils.isBlank(response.getQrCode())) {
            throw new BusinessException("支付宝下单失败：" + describe(response));
        }
        return response.getQrCode();
    }

    /**
     * 手机网站支付：返回需要浏览器自动提交的 HTML 表单
     */
    public String wapForm(String orderNo, String subject, String totalAmount, String quitUrl) {
        if (isMock()) {
            return mockForm("QUICK_WAP_WAY", orderNo, totalAmount, subject);
        }
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(orderNo);
        model.setSubject(subject);
        model.setTotalAmount(totalAmount);
        model.setProductCode("QUICK_WAP_WAY");
        model.setTimeoutExpress(properties.getTimeoutExpress());
        if (StringUtils.isNotBlank(quitUrl)) {
            model.setQuitUrl(quitUrl);
        }
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(properties.getNotifyUrl());
        if (StringUtils.isNotBlank(properties.getReturnUrl())) {
            request.setReturnUrl(properties.getReturnUrl());
        }
        AlipayTradeWapPayResponse response = (AlipayTradeWapPayResponse) call(request, true);
        if (response == null || StringUtils.isBlank(response.getBody())) {
            throw new BusinessException("支付宝手机网站下单失败：" + describe(response));
        }
        return response.getBody();
    }

    /**
     * 电脑网站支付：返回需要浏览器自动提交的 HTML 表单
     */
    public String pageForm(String orderNo, String subject, String totalAmount) {
        if (isMock()) {
            return mockForm("FAST_INSTANT_TRADE_PAY", orderNo, totalAmount, subject);
        }
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setSubject(subject);
        model.setTotalAmount(totalAmount);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeoutExpress(properties.getTimeoutExpress());
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(properties.getNotifyUrl());
        if (StringUtils.isNotBlank(properties.getReturnUrl())) {
            request.setReturnUrl(properties.getReturnUrl());
        }
        AlipayTradePagePayResponse response = (AlipayTradePagePayResponse) call(request, true);
        if (response == null || StringUtils.isBlank(response.getBody())) {
            throw new BusinessException("支付宝电脑网站下单失败：" + describe(response));
        }
        return response.getBody();
    }

    /**
     * 交易查询
     */
    public AlipayTradeQueryResponse query(String orderNo) {
        if (isMock()) {
            return null;
        }
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        AlipayTradeQueryResponse response = (AlipayTradeQueryResponse) call(request, false);
        if (response == null) {
            throw new BusinessException("支付宝查单失败");
        }
        return response;
    }

    /**
     * 关闭交易
     */
    public void close(String orderNo) {
        if (isMock()) {
            return;
        }
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderNo);
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);
        AlipayTradeCloseResponse response = (AlipayTradeCloseResponse) call(request, false);
        if (response != null && !response.isSuccess()) {
            String subCode = response.getSubCode();
            if ("ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                return;
            }
            throw new BusinessException("关闭支付宝订单失败：" + describe(response));
        }
    }

    /**
     * 退款。outRequestNo 用于区分同一订单的多次部分退款，必须唯一。
     */
    public AlipayTradeRefundResponse refund(String orderNo, String outRequestNo, String refundAmount, String reason) {
        if (isMock()) {
            return null;
        }
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(orderNo);
        model.setOutRequestNo(outRequestNo);
        model.setRefundAmount(refundAmount);
        if (StringUtils.isNotBlank(reason)) {
            model.setRefundReason(reason);
        }
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        AlipayTradeRefundResponse response = (AlipayTradeRefundResponse) call(request, false);
        if (response == null || !response.isSuccess()) {
            throw new BusinessException("支付宝退款失败：" + describe(response));
        }
        return response;
    }

    /**
     * 退款查询
     */
    public AlipayTradeFastpayRefundQueryResponse refundQuery(String orderNo, String outRequestNo) {
        if (isMock()) {
            return null;
        }
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(orderNo);
        model.setOutRequestNo(outRequestNo);
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);
        AlipayTradeFastpayRefundQueryResponse response =
                (AlipayTradeFastpayRefundQueryResponse) call(request, false);
        if (response == null || !response.isSuccess()) {
            throw new BusinessException("支付宝退款查询失败：" + describe(response));
        }
        return response;
    }

    /**
     * 对账单下载地址
     *
     * @param billDate yyyy-MM-dd
     * @param billType trade（交易账单）| signcustomer（资金账单）
     */
    public String billDownloadUrl(String billDate, String billType) {
        if (isMock()) {
            return null;
        }
        com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel model =
                new com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel();
        model.setBillDate(billDate);
        model.setBillType(billType);
        AlipayDataDataserviceBillDownloadurlQueryRequest request =
                new AlipayDataDataserviceBillDownloadurlQueryRequest();
        request.setBizModel(model);
        AlipayDataDataserviceBillDownloadurlQueryResponse response =
                (AlipayDataDataserviceBillDownloadurlQueryResponse) call(request, false);
        if (response == null || !response.isSuccess()) {
            throw new BusinessException("获取支付宝对账单失败：" + describe(response));
        }
        return response.getBillDownloadUrl();
    }

    /**
     * 异步通知验签
     */
    public boolean verifyNotify(Map<String, String> params) {
        if (isMock()) {
            return true;
        }
        ensureReady();
        try {
            if (isCertMode()) {
                return AlipaySignature.rsaCertCheckV2(params, properties.getAlipayPublicCertPath(),
                        properties.getCharset(), properties.getSignType());
            }
            return AlipaySignature.rsaCheckV2(params, properties.getAlipayPublicKey(),
                    properties.getCharset(), properties.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
            return false;
        }
    }

    /**
     * @param pageRequest true 表示页面类请求（返回 HTML 表单），用 pageExecute
     */
    private Object call(com.alipay.api.AlipayRequest<?> request, boolean pageRequest) {
        ensureReady();
        try {
            if (pageRequest) {
                return getClient().pageExecute(request);
            }
            if (isCertMode()) {
                return getClient().certificateExecute(request);
            }
            return getClient().execute(request);
        } catch (AlipayApiException e) {
            log.error("调用支付宝失败, api={}, msg={}", request.getApiMethodName(), e.getErrMsg(), e);
            throw new BusinessException("调用支付宝失败：" + e.getErrMsg());
        }
    }

    private void ensureReady() {
        if (isMock()) {
            return;
        }
        if (!isEnabled()) {
            throw new BusinessException("支付宝支付未启用，请在 sa-base.yaml 中设置 alipay.pay.enabled=true");
        }
        if (!isConfigured()) {
            throw new BusinessException("支付宝配置不完整，请检查 app-id、私钥、"
                    + (isCertMode() ? "三个证书路径" : "支付宝公钥") + "、notify-url");
        }
    }

    private com.alipay.api.AlipayClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = buildClient();
                }
            }
        }
        return client;
    }

    private com.alipay.api.AlipayClient buildClient() {
        String serverUrl = properties.resolveGatewayUrl();
        String privateKey = readPrivateKey();
        try {
            if (isCertMode()) {
                checkFile(properties.getAppCertPath(), "应用公钥证书");
                checkFile(properties.getAlipayPublicCertPath(), "支付宝公钥证书");
                checkFile(properties.getAlipayRootCertPath(), "支付宝根证书");
                CertAlipayRequest certRequest = new CertAlipayRequest();
                certRequest.setServerUrl(serverUrl);
                certRequest.setAppId(properties.getAppId());
                certRequest.setPrivateKey(privateKey);
                certRequest.setFormat("json");
                certRequest.setCharset(properties.getCharset());
                certRequest.setSignType(properties.getSignType());
                certRequest.setCertPath(properties.getAppCertPath());
                certRequest.setAlipayPublicCertPath(properties.getAlipayPublicCertPath());
                certRequest.setRootCertPath(properties.getAlipayRootCertPath());
                return new DefaultAlipayClient(certRequest);
            }
            AlipayConfig config = new AlipayConfig();
            config.setServerUrl(serverUrl);
            config.setAppId(properties.getAppId());
            config.setPrivateKey(privateKey);
            config.setFormat("json");
            config.setCharset(properties.getCharset());
            config.setSignType(properties.getSignType());
            config.setAlipayPublicKey(properties.getAlipayPublicKey());
            return new DefaultAlipayClient(config);
        } catch (Exception e) {
            log.error("初始化支付宝客户端失败", e);
            throw new BusinessException("初始化支付宝失败：" + e.getMessage());
        }
    }

    private String readPrivateKey() {
        if (StringUtils.isNotBlank(properties.getPrivateKey())) {
            return normalizePrivateKey(properties.getPrivateKey());
        }
        try {
            return new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(properties.getPrivateKeyPath())),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("读取支付宝应用私钥失败：" + e.getMessage());
        }
    }

    /**
     * 把 yaml 里的 \n 还原成真正的换行，并去掉 PEM 头尾（SDK 只要 Base64 主体）
     */
    private String normalizePrivateKey(String key) {
        String k = key.replace("\\n", "\n").trim();
        k = k.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "");
        return k.replaceAll("\\s", "");
    }

    private void checkFile(String path, String label) {
        if (StringUtils.isBlank(path) || !new File(path).exists()) {
            throw new BusinessException(label + "不存在：" + path);
        }
    }

    private String describe(com.alipay.api.AlipayResponse response) {
        if (response == null) {
            return "无响应";
        }
        return StringUtils.defaultString(response.getSubCode())
                + " " + StringUtils.defaultString(response.getSubMsg());
    }

    private String mockForm(String productCode, String orderNo, String totalAmount, String subject) {
        return "<form id=\"alipay-mock\" action=\"" + properties.resolveGatewayUrl() + "\" method=\"post\">"
                + "<input name=\"mock\" value=\"1\"/>"
                + "<input name=\"out_trade_no\" value=\"" + orderNo + "\"/>"
                + "<input name=\"total_amount\" value=\"" + totalAmount + "\"/>"
                + "<input name=\"subject\" value=\"" + subject + "\"/>"
                + "<input name=\"product_code\" value=\"" + productCode + "\"/>"
                + "</form><script>document.forms[0].submit()</script>";
    }
}
