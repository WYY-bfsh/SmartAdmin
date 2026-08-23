package net.lab1024.sa.admin.module.business.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayTradeTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单
 */
@Data
public class PayOrderVO {

    @Schema(description = "支付订单ID")
    private Long payOrderId;

    @Schema(description = "商户订单号")
    private String orderNo;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "订单金额（分）")
    private Integer amount;

    @Schema(description = "订单金额（元）")
    private BigDecimal amountYuan;

    @SchemaEnum(PayTradeTypeEnum.class)
    private Integer tradeType;

    @SchemaEnum(PayStatusEnum.class)
    private Integer payStatus;

    @Schema(description = "二维码内容")
    private String codeUrl;

    @Schema(description = "微信支付订单号")
    private String transactionId;

    @Schema(description = "用户标识")
    private String openid;

    @Schema(description = "用户实付（分）")
    private Integer payerTotal;

    @Schema(description = "支付成功时间")
    private LocalDateTime successTime;

    @Schema(description = "商户退款单号")
    private String refundNo;

    @Schema(description = "已退款金额（分）")
    private Integer refundAmount;

    @Schema(description = "已退款金额（元）")
    private BigDecimal refundAmountYuan;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

    @Schema(description = "备注")
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
