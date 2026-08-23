package net.lab1024.sa.admin.module.business.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单回复消息
 */
@Data
@TableName("t_cs_ticket_message")
public class TicketMessageEntity {

    @TableId(type = IdType.AUTO)
    private Long messageId;

    /** 工单ID */
    private Long ticketId;

    /** 类型：1客户消息 2客服回复 3系统消息 */
    private Integer messageType;

    /** 消息内容 */
    private String content;

    /** 发送人（员工ID） */
    private Long createUserId;

    /** 发送人名称 */
    private String createName;

    private Boolean deletedFlag;

    private LocalDateTime createTime;
}