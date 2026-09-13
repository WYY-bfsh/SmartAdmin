package net.lab1024.sa.admin.module.business.pay.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付商户配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WeChatPayProperties {

    public static final String DEMO_APP_ID = "wx8f3a2c1d4e5b6789";

    public static final String DEMO_MCH_ID = "1639284750";

    public static final String DEMO_API_V3_KEY = "SmartAdminWxPayDemoKey32Chars!!";

    public static final String DEMO_SERIAL_NUMBER = "5A8C2E1B9D4F60783C1A0E6B2D9F4C7A8E1B3D5F";

    public static final String DEMO_PRIVATE_KEY = "DEMO_MOCK_PRIVATE_KEY";

    public static final String DEMO_NOTIFY_URL = "https://demo.smartadmin.local/pay/wechat/notify";

    /**
     * 是否启用。为 false 时不会初始化 SDK，也不允许下单。
     */
    private Boolean enabled = false;

    /**
     * 没有真实商户号时开启演示模式：不请求微信，使用本地示例数据。
     */
    private Boolean mock = false;

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

    /**
     * 公众号 AppSecret，仅微信内 JSAPI 网页授权取 openid 时需要
     */
    private String appSecret;

    /**
     * H5 支付 scene_info.app_url，例如 https://desire.wang
     */
    private String h5AppUrl;

    @PostConstruct
    public void applyDemoDefaults() {
        if (!Boolean.TRUE.equals(mock)) {
            return;
        }
        if (StringUtils.isBlank(appId)) {
            appId = DEMO_APP_ID;
        }
        if (StringUtils.isBlank(mchId)) {
            mchId = DEMO_MCH_ID;
        }
        if (StringUtils.isBlank(apiV3Key)) {
            apiV3Key = DEMO_API_V3_KEY;
        }
        if (StringUtils.isBlank(merchantSerialNumber)) {
            merchantSerialNumber = DEMO_SERIAL_NUMBER;
        }
        if (StringUtils.isBlank(privateKey) && StringUtils.isBlank(privateKeyPath)) {
            privateKey = DEMO_PRIVATE_KEY;
        }
        if (StringUtils.isBlank(notifyUrl)) {
            notifyUrl = DEMO_NOTIFY_URL;
        }
        if (!Boolean.TRUE.equals(enabled)) {
            enabled = true;
        }
    }
}
