package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 发起微信支付
 */
@Data
public class PayOrderCreateForm {

    @Schema(description = "商品描述")
    @NotBlank(message = "商品描述不能为空")
    @Length(max = 127, message = "商品描述最多127个字符")
    private String description;

    @Schema(description = "支付金额（元）")
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额最低0.01元")
    @DecimalMax(value = "1000000", message = "支付金额超出限制")
    private BigDecimal amountYuan;

    @Schema(description = "支付渠道 1微信 2支付宝，默认微信")
    @CheckEnum(value = PayChannelEnum.class, required = false, message = "支付渠道错误")
    private Integer payChannel;

    @Schema(description = "备注")
    @Length(max = 200, message = "备注最多200个字符")
    private String remark;
}
