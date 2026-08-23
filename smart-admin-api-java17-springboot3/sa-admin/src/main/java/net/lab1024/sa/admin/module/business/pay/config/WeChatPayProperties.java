package net.lab1024.sa.admin.module.business.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付商户配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WeChatPayProperties {

    /**
     * 是否启用。为 false 时不会初始化 SDK，也不允许下单。
     */
    private Boolean enabled = false;

    /**
     * 公众号/小程序/开放平台 AppId
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * APIv3 密钥（32位）
     */
    private String apiV3Key;

    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 商户私钥 PEM 内容（与 privateKeyPath 二选一）
     */
    private String privateKey;

    /**
     * 商户私钥文件路径，例如 D:/cert/apiclient_key.pem
     */
    private String privateKeyPath;

    /**
     * 支付结果通知地址，必须是公网 HTTPS
     */
    private String notifyUrl;
}
