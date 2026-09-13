package net.lab1024.sa.admin.module.business.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 支付宝配置概览（不返回私钥）
 */
@Data
public class AlipayConfigVO {

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "是否演示模式")
    private Boolean mock;

    @Schema(description = "是否沙箱")
    private Boolean sandbox;

    @Schema(description = "加签模式 public-key | cert")
    private String signMode;

    @Schema(description = "配置是否完整")
    private Boolean configured;

    @Schema(description = "AppId（脱敏）")
    private String appId;

    @Schema(description = "当前使用的网关")
    private String gatewayUrl;

    @Schema(description = "异步通知地址")
    private String notifyUrl;

    @Schema(description = "私钥是否已配置")
    private Boolean privateKeyReady;
}
