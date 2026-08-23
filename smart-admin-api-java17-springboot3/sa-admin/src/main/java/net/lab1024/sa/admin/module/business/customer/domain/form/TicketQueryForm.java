package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.TicketPriorityEnum;
import net.lab1024.sa.admin.module.business.customer.constant.TicketStatusEnum;
import net.lab1024.sa.admin.module.business.customer.constant.TicketTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * 工单分页查询
 */
@Data
public class TicketQueryForm extends PageParam {

    @Schema(description = "工单编号")
    @Length(max = 32, message = "工单编号最多32个字符")
    private String ticketNo;

    @Schema(description = "工单标题")
    @Length(max = 200, message = "标题最多200个字符")
    private String title;

    @SchemaEnum(TicketTypeEnum.class)
    @CheckEnum(value = TicketTypeEnum.class, required = false, message = "工单类型错误")
    private Integer ticketType;

    @SchemaEnum(TicketPriorityEnum.class)
    @CheckEnum(value = TicketPriorityEnum.class, required = false, message = "优先级错误")
    private Integer priority;

    @SchemaEnum(TicketStatusEnum.class)
    @CheckEnum(value = TicketStatusEnum.class, required = false, message = "工单状态错误")
    private Integer status;

    @Schema(description = "创建-开始日期")
    private LocalDate createTimeBegin;

    @Schema(description = "创建-截止日期")
    private LocalDate createTimeEnd;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}