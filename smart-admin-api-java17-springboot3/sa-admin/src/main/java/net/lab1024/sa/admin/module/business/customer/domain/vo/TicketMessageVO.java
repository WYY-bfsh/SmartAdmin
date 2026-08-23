package net.lab1024.sa.admin.module.business.customer.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.TicketMessageTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 工单回复消息
 */
@Data
public class TicketMessageVO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "工单ID")
    private Long ticketId;

    @SchemaEnum(TicketMessageTypeEnum.class)
    private Integer messageType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "发送人ID")
    private Long createUserId;

    @Schema(description = "发送人名称")
    private String createName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}