package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconBatchStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconSourceEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

import java.time.LocalDate;

/**
 * 对账批次分页
 */
@Data
public class PayReconBatchQueryForm extends PageParam {

    @Schema(description = "账单日期")
    private LocalDate billDate;

    @SchemaEnum(PayChannelEnum.class)
    @CheckEnum(value = PayChannelEnum.class, required = false, message = "支付渠道错误")
    private Integer payChannel;

    @SchemaEnum(PayReconSourceEnum.class)
    @CheckEnum(value = PayReconSourceEnum.class, required = false, message = "来源错误")
    private Integer sourceType;

    @SchemaEnum(PayReconBatchStatusEnum.class)
    @CheckEnum(value = PayReconBatchStatusEnum.class, required = false, message = "批次状态错误")
    private Integer batchStatus;
}
