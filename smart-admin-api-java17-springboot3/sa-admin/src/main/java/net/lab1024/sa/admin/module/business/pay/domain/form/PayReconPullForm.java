package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

import java.time.LocalDate;

/**
 * 拉取/演示对账
 */
@Data
public class PayReconPullForm {

    @NotNull(message = "账单日期不能为空")
    @Schema(description = "账单日期")
    private LocalDate billDate;

    @NotNull(message = "支付渠道不能为空")
    @SchemaEnum(PayChannelEnum.class)
    @CheckEnum(value = PayChannelEnum.class, message = "支付渠道错误")
    private Integer payChannel;
}
