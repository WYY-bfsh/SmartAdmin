package net.lab1024.sa.admin.module.business.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "秒杀单微信支付参数")
public class MallWechatPayVO {

    @Schema(description = "是否已启用真实/演示微信支付")
    private Boolean wechatReady;

    @Schema(description = "演示模式")
    private Boolean mock;

    @Schema(description = "native / jsapi / h5")
    private String tradeType;

    private String codeUrl;

    private String qrcodeBase64;

    private String h5Url;

    private String jsapiAppId;

    private String jsapiTimeStamp;

    private String jsapiNonceStr;

    private String jsapiPackage;

    private String jsapiSignType;

    private String jsapiPaySign;

    private Boolean needOpenid;
}
