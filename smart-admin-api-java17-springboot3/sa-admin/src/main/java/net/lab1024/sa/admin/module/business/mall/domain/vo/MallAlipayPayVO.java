package net.lab1024.sa.admin.module.business.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "秒杀单支付宝支付参数")
public class MallAlipayPayVO {

    private Boolean alipayReady;

    private Boolean mock;

    private String tradeType;

    private String codeUrl;

    private String qrcodeBase64;

    private Long payOrderId;
}
