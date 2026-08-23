package net.lab1024.sa.admin.module.business.customer.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.TicketPriorityEnum;
import net.lab1024.sa.admin.module.business.customer.constant.TicketStatusEnum;
import net.lab1024.sa.admin.module.business.customer.constant.TicketTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单
 */
@Data
public class TicketVO {

    @Schema(description = "工单ID")
    private Long ticketId;

    @Schema(description = "工单编号")
    private String ticketNo;

    @Schema(description = "工单标题")
    private String title;

    @SchemaEnum(TicketTypeEnum.class)
    private Integer ticketType;

    @SchemaEnum(TicketPriorityEnum.class)
    private Integer priority;

    @SchemaEnum(TicketStatusEnum.class)
    private Integer status;

    @Schema(description = "工单内容")
    private String content;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "处理人ID")
    private Long handlerUserId;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

    @Schema(description = "回复次数")
    private Integer replyCount;

    @Schema(description = "回复消息列表")
    private List<TicketMessageVO> messageList;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}