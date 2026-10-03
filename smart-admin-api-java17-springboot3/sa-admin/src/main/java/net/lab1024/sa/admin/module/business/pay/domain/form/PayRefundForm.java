package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 申请退款
 */
@Data
public class PayRefundForm {

    @Schema(description = "支付订单ID")
    @NotNull(message = "支付订单不能为空")
    private Long payOrderId;

    @Schema(description = "退款金额（元）")
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额最低0.01元")
    private BigDecimal refundAmountYuan;

    @Schema(description = "退款原因")
    @Length(max = 80, message = "退款原因最多80个字符")
    private String reason;
}
