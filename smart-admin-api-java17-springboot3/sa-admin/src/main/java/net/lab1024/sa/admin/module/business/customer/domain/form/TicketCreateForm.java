package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.TicketPriorityEnum;
import net.lab1024.sa.admin.module.business.customer.constant.TicketTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 创建工单
 */
@Data
public class TicketCreateForm {

    @Schema(description = "工单标题")
    @NotBlank(message = "工单标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @SchemaEnum(TicketTypeEnum.class)
    @CheckEnum(value = TicketTypeEnum.class, required = true, message = "工单类型错误")
    private Integer ticketType;

    @SchemaEnum(TicketPriorityEnum.class)
    @CheckEnum(value = TicketPriorityEnum.class, required = true, message = "优先级错误")
    private Integer priority;

    @Schema(description = "工单内容")
    @NotBlank(message = "工单内容不能为空")
    private String content;

    @Schema(description = "联系人")
    @Size(max = 50, message = "联系人最多50个字符")
    private String contactName;

    @Schema(description = "联系电话")
    @Size(max = 20, message = "联系电话最多20个字符")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    @Size(max = 100, message = "联系邮箱最多100个字符")
    private String contactEmail;
}