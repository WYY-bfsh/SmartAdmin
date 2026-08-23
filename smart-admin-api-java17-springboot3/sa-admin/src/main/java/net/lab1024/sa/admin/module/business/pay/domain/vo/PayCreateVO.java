package net.lab1024.sa.admin.module.business.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发起支付结果
 */
@Data
public class PayCreateVO {

    @Schema(description = "支付订单ID")
    private Long payOrderId;

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "支付金额（元）")
    private BigDecimal amountYuan;

    @Schema(description = "二维码内容")
    private String codeUrl;

    @Schema(description = "二维码图片 Base64")
    private String qrcodeBase64;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "是否演示模式")
    private Boolean mock;
}
