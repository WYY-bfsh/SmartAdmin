package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MallWechatPrepayForm {

    @NotNull
    private Long orderId;

    @Schema(description = "native / jsapi / h5，空则按端自动")
    private String tradeType;

    @Schema(description = "JSAPI 必填，也可先走授权接口写入会员")
    private String openid;

    @Schema(description = "H5 支付客户端 IP")
    private String clientIp;
}
