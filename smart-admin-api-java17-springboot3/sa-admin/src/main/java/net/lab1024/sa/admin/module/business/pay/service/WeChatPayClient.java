package net.lab1024.sa.admin.module.business.pay.service;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.pay.config.WeChatPayProperties;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 微信支付官方 SDK 封装，配置不完整时不会初始化。
 */
@Slf4j
@Component
public class WeChatPayClient {

    private static final DateTimeFormatter TIME_EXPIRE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Resource
    private WeChatPayProperties properties;

    private volatile Config config;

    private volatile NativePayService nativePayService;

    private volatile RefundService refundService;

    private volatile NotificationParser notificationParser;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getEnabled()) || isMock();
    }

    public boolean isMock() {
        return Boolean.TRUE.equals(properties.getMock());
    }

    public boolean isConfigured() {
        if (isMock()) {
            return true;
        }
        return StringUtils.isNoneBlank(properties.getAppId(), properties.getMchId(), properties.getApiV3Key(),
                properties.getMerchantSerialNumber(), properties.getNotifyUrl())
                && (StringUtils.isNotBlank(properties.getPrivateKey()) || StringUtils.isNotBlank(properties.getPrivateKeyPath()));
    }

    public WeChatPayProperties getProperties() {
        return properties;
    }

    public String prepayNative(String orderNo, String description, int amountFen) {
        if (isMock()) {
            return "weixin://wxpay/bizpayurl?pr=DEMO" + orderNo;
        }
        NativePayService service = getNativePayService();
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(amountFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        request.setAppid(properties.getAppId());
        request.setMchid(properties.getMchId());
        request.setDescription(description);
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setOutTradeNo(orderNo);
        request.setTimeExpire(OffsetDateTime.now(ZoneId.of("Asia/Shanghai")).plusMinutes(30).format(TIME_EXPIRE_FORMAT));
        PrepayResponse response = service.prepay(request);
        if (response == null || StringUtils.isBlank(response.getCodeUrl())) {
            throw new BusinessException("微信下单失败，未返回支付二维码");
        }
        return response.getCodeUrl();
    }

    public Transaction queryByOutTradeNo(String orderNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(properties.getMchId());
        request.setOutTradeNo(orderNo);
        return getNativePayService().queryOrderByOutTradeNo(request);
    }

    public void closeOrder(String orderNo) {
        if (isMock()) {
            return;
        }
        CloseOrderRequest request = new CloseOrderRequest();
        request.setMchid(properties.getMchId());
        request.setOutTradeNo(orderNo);
        getNativePayService().closeOrder(request);
    }

    public Refund refund(String orderNo, String refundNo, int totalFen, int refundFen, String reason) {
        CreateRequest request = new CreateRequest();
        AmountReq amount = new AmountReq();
        amount.setTotal((long) totalFen);
        amount.setRefund((long) refundFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        request.setOutTradeNo(orderNo);
        request.setOutRefundNo(refundNo);
        if (StringUtils.isNotBlank(reason)) {
            request.setReason(reason);
        }
        return getRefundService().create(request);
    }

    public Transaction parsePayNotify(RequestParam requestParam) {
        return getNotificationParser().parse(requestParam, Transaction.class);
    }

    private NativePayService getNativePayService() {
        ensureReady();
        if (nativePayService == null) {
            synchronized (this) {
                if (nativePayService == null) {
                    nativePayService = new NativePayService.Builder().config(getConfig()).build();
                }
            }
        }
        return nativePayService;
    }

    private RefundService getRefundService() {
        ensureReady();
        if (refundService == null) {
            synchronized (this) {
                if (refundService == null) {
                    refundService = new RefundService.Builder().config(getConfig()).build();
                }
            }
        }
        return refundService;
    }

    private NotificationParser getNotificationParser() {
        ensureReady();
        if (notificationParser == null) {
            synchronized (this) {
                if (notificationParser == null) {
                    notificationParser = new NotificationParser((RSAAutoCertificateConfig) getConfig());
                }
            }
        }
        return notificationParser;
    }

    private Config getConfig() {
        if (config == null) {
            synchronized (this) {
                if (config == null) {
                    config = buildConfig();
                }
            }
        }
        return config;
    }

    private void ensureReady() {
        if (isMock()) {
            return;
        }
        if (!isEnabled()) {
            throw new BusinessException("微信支付未启用，请在 sa-base.yaml 中设置 wechat.pay.enabled=true");
        }
        if (!isConfigured()) {
            throw new BusinessException("微信支付商户信息不完整，请填写 app-id、mch-id、api-v3-key、证书序列号、私钥和 notify-url");
        }
    }

    private Config buildConfig() {
        RSAAutoCertificateConfig.Builder builder = new RSAAutoCertificateConfig.Builder()
                .merchantId(properties.getMchId())
                .merchantSerialNumber(properties.getMerchantSerialNumber())
                .apiV3Key(properties.getApiV3Key());
        if (StringUtils.isNotBlank(properties.getPrivateKey())) {
            builder.privateKey(normalizePrivateKey(properties.getPrivateKey()));
        } else {
            builder.privateKeyFromPath(properties.getPrivateKeyPath());
        }
        try {
            return builder.build();
        } catch (Exception e) {
            log.error("初始化微信支付客户端失败", e);
            throw new BusinessException("初始化微信支付失败：" + e.getMessage());
        }
    }

    private String normalizePrivateKey(String privateKey) {
        return privateKey.replace("\\n", "\n");
    }
}
