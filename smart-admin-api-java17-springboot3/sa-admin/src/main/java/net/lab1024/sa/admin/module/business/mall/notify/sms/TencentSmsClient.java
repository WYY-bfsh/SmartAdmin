package net.lab1024.sa.admin.module.business.mall.notify.sms;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.notify.config.TencentSmsProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Tencent Cloud SMS sender. When mock=true or credentials incomplete, logs and returns MOCK success
 * so personal/dev can finish MQ/idempotent flow without enterprise SMS qualification.
 */
@Slf4j
@Component
public class TencentSmsClient {

    @Resource
    private TencentSmsProperties properties;

    public SmsSendResult sendOrderCreated(String mobile, String orderNo, String amountText) {
        String phone = normalizePhone(mobile);
        String content = "您的订单 " + orderNo + " 已创建成功，金额 " + amountText + " 元。";

        if (!properties.isEnabled()) {
            return mockResult(phone, orderNo, content, "tencent.sms.enabled=false");
        }
        if (properties.isMock() || !isFullyConfigured()) {
            return mockResult(phone, orderNo, content, properties.isMock() ? "mock=true" : "config incomplete");
        }

        try {
            Credential cred = new Credential(properties.getSecretId(), properties.getSecretKey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            SmsClient client = new SmsClient(cred, properties.getRegion(), clientProfile);
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(properties.getSdkAppId());
            req.setSignName(properties.getSignName());
            req.setTemplateId(properties.getTemplateId());
            req.setPhoneNumberSet(new String[]{phone});
            req.setTemplateParamSet(new String[]{orderNo, amountText});

            SendSmsResponse resp = client.SendSms(req);
            String requestId = resp.getRequestId();
            SendStatus[] statuses = resp.getSendStatusSet();
            if (statuses != null && statuses.length > 0) {
                SendStatus st = statuses[0];
                String code = st.getCode();
                if ("Ok".equalsIgnoreCase(code)) {
                    SmsSendResult ok = new SmsSendResult();
                    ok.setSuccess(true);
                    ok.setRequestId(requestId);
                    ok.setSerialNo(st.getSerialNo());
                    ok.setMessage(st.getMessage());
                    ok.setMock(false);
                    return ok;
                }
                SmsSendResult fail = new SmsSendResult();
                fail.setSuccess(false);
                fail.setRequestId(requestId);
                fail.setMessage(code + ": " + st.getMessage());
                fail.setMock(false);
                return fail;
            }
            SmsSendResult empty = new SmsSendResult();
            empty.setSuccess(false);
            empty.setRequestId(requestId);
            empty.setMessage("Empty SendStatusSet");
            empty.setMock(false);
            return empty;
        } catch (TencentCloudSDKException e) {
            log.error("[order.notify] Tencent SMS SDK error: {}", e.getMessage());
            SmsSendResult fail = new SmsSendResult();
            fail.setSuccess(false);
            fail.setRequestId(e.getRequestId());
            fail.setMessage(e.getMessage());
            fail.setMock(false);
            return fail;
        }
    }

    private SmsSendResult mockResult(String phone, String orderNo, String content, String reason) {
        String rid = "MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("[order.notify][MOCK SMS] reason={} phone={} orderNo={} content={} requestId={}",
                reason, phone, orderNo, content, rid);
        SmsSendResult ok = new SmsSendResult();
        ok.setSuccess(true);
        ok.setRequestId(rid);
        ok.setSerialNo(rid);
        ok.setMessage("MOCK_OK:" + reason);
        ok.setMock(true);
        return ok;
    }

    private boolean isFullyConfigured() {
        return StringUtils.hasText(properties.getSecretId())
                && StringUtils.hasText(properties.getSecretKey())
                && StringUtils.hasText(properties.getSdkAppId())
                && StringUtils.hasText(properties.getSignName())
                && StringUtils.hasText(properties.getTemplateId());
    }

    private static String normalizePhone(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            throw new IllegalArgumentException("blank mobile");
        }
        String m = mobile.trim();
        if (m.startsWith("+")) {
            return m;
        }
        if (m.startsWith("86") && m.length() > 11) {
            return "+" + m;
        }
        return "+86" + m;
    }

    @Data
    public static class SmsSendResult {
        private boolean success;
        private String requestId;
        private String serialNo;
        private String message;
        private boolean mock;
    }
}