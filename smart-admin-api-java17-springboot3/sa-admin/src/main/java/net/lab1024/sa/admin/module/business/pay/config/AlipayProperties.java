package net.lab1024.sa.admin.module.business.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置
 * <p>
 * sign-mode = public-key 时只需 private-key + alipay-public-key；
 * sign-mode = cert 时需 private-key + 三个证书路径。
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay.pay")
public class AlipayProperties {

    public static final String GATEWAY_PROD = "https://openapi.alipay.com/gateway.do";

    public static final String GATEWAY_SANDBOX = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 是否启用。false 时不允许下单 */
    private Boolean enabled = false;

    /** 演示模式：不请求支付宝，用本地示例数据（联调前端用） */
    private Boolean mock = false;

    /** 是否沙箱环境 */
    private Boolean sandbox = true;

    /** 加签模式：public-key | cert */
    private String signMode = "public-key";

    /** 支付宝网关，留空则按 sandbox 自动取 */
    private String gatewayUrl;

    private String appId;

    /** 应用私钥（PKCS8 格式，一行 Base64，不要带头尾标记） */
    private String privateKey;

    /** 应用私钥文件路径，与 privateKey 二选一 */
    private String privateKeyPath;

    /** 支付宝公钥（仅 public-key 模式使用） */
    private String alipayPublicKey;

    /** 应用公钥证书路径（仅 cert 模式） */
    private String appCertPath;

    /** 支付宝公钥证书路径（仅 cert 模式） */
    private String alipayPublicCertPath;

    /** 支付宝根证书路径（仅 cert 模式） */
    private String alipayRootCertPath;

    /** 异步通知地址，必须是公网 HTTPS 且不带参数，形如 https://域名/api/pay/alipay/notify */
    private String notifyUrl;

    /** 同步跳转地址（页面支付完成后回到哪里） */
    private String returnUrl;

    private String charset = "UTF-8";

    private String signType = "RSA2";

    /** 订单超时，如 30m */
    private String timeoutExpress = "30m";

    private Integer connectTimeout = 5000;

    private Integer readTimeout = 15000;

    public boolean isCertMode() {
        return "cert".equalsIgnoreCase(signMode);
    }

    public String resolveGatewayUrl() {
        if (gatewayUrl != null && !gatewayUrl.isBlank()) {
            return gatewayUrl.trim();
        }
        return Boolean.TRUE.equals(sandbox) ? GATEWAY_SANDBOX : GATEWAY_PROD;
    }
}
