package net.lab1024.sa.admin.module.business.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服问答
 */
@Data
@TableName("t_cs_qa")
public class QAEntity {

    @TableId(type = IdType.AUTO)
    private Long qaId;

    /** 提问内容 */
    private String question;

    /** 回答内容 */
    private String answer;

    /** 状态：10待回答 20已回答 30已驳回 */
    private Integer status;

    /** 提问人 */
    private String askerName;

    /** 提问人电话 */
    private String askerPhone;

    /** 提问人邮箱 */
    private String askerEmail;

    /** 创建人（员工ID） */
    private Long createUserId;

    /** 回答人 */
    private Long answerUserId;

    /** 回答时间 */
    private LocalDateTime answerTime;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}