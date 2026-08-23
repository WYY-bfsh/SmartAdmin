package net.lab1024.sa.admin.module.business.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服工单
 */
@Data
@TableName("t_cs_ticket")
public class TicketEntity {

    @TableId(type = IdType.AUTO)
    private Long ticketId;

    /** 工单编号 */
    private String ticketNo;

    /** 工单标题 */
    private String title;

    /** 类型：1问题咨询 2功能建议 3Bug反馈 4业务查询 5其他 */
    private Integer ticketType;

    /** 优先级：1低 2中 3高 4紧急 */
    private Integer priority;

    /** 状态：10待处理 20处理中 30已回复 40已关闭 */
    private Integer status;

    /** 工单内容 */
    private String content;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 联系邮箱 */
    private String contactEmail;

    /** 创建人（员工ID） */
    private Long createUserId;

    /** 处理人 */
    private Long handlerUserId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 关闭时间 */
    private LocalDateTime closeTime;

    /** 回复次数 */
    private Integer replyCount;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}