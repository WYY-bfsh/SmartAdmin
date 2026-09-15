package net.lab1024.sa.admin.module.business.mall.notify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tencent Cloud SMS config. mock=true (default for personal/dev) skips real API.
 */
@Data
@Component
@ConfigurationProperties(prefix = "tencent.sms")
public class TencentSmsProperties {

    private boolean enabled = true;

    /** When true or credentials incomplete, TencentSmsClient returns MOCK success. */
    private boolean mock = true;

    private String secretId;

    private String secretKey;

    private String sdkAppId;

    private String signName;

    private String templateId;

    private String region = "ap-guangzhou";
}