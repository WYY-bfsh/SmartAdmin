package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconMatchStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 对账明细分页
 */
@Data
public class PayReconItemQueryForm extends PageParam {

    @NotNull(message = "批次不能为空")
    @Schema(description = "批次ID")
    private Long batchId;

    @SchemaEnum(PayReconMatchStatusEnum.class)
    @CheckEnum(value = PayReconMatchStatusEnum.class, required = false, message = "匹配结果错误")
    private Integer matchStatus;

    @Schema(description = "商户订单号")
    @Length(max = 64)
    private String orderNo;

    @Schema(description = "是否已核销")
    private Boolean handledFlag;
}
