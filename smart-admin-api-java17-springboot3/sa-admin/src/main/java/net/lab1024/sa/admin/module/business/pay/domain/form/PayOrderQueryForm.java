package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * 支付订单分页查询
 */
@Data
public class PayOrderQueryForm extends PageParam {

    @Schema(description = "商户订单号")
    @Length(max = 32, message = "订单号最多32个字符")
    private String orderNo;

    @SchemaEnum(PayStatusEnum.class)
    @CheckEnum(value = PayStatusEnum.class, required = false, message = "支付状态错误")
    private Integer payStatus;

    @Schema(description = "创建-开始日期")
    private LocalDate createTimeBegin;

    @Schema(description = "创建-截止日期")
    private LocalDate createTimeEnd;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}
