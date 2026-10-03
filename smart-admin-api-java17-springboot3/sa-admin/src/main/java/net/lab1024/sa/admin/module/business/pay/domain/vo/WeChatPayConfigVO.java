package net.lab1024.sa.admin.module.business.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信支付配置概览（不返回密钥）
 */
@Data
public class WeChatPayConfigVO {

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "是否演示模式（无真实商户号）")
    private Boolean mock;

    @Schema(description = "商户资料是否填写完整")
    private Boolean configured;

    @Schema(description = "AppId（脱敏）")
    private String appId;

    @Schema(description = "商户号")
    private String mchId;

    @Schema(description = "支付回调地址")
    private String notifyUrl;

    @Schema(description = "私钥是否已配置")
    private Boolean privateKeyReady;

    @Schema(description = "公众号 AppSecret 是否已配置（微信内 JSAPI 授权）")
    private Boolean appSecretReady;

    @Schema(description = "H5 支付 app_url")
    private String h5AppUrl;
}
