package net.lab1024.sa.admin.module.business.customer.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.QAStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 问答
 */
@Data
public class QAVO {

    @Schema(description = "问答ID")
    private Long qaId;

    @Schema(description = "提问内容")
    private String question;

    @Schema(description = "回答内容")
    private String answer;

    @SchemaEnum(QAStatusEnum.class)
    private Integer status;

    @Schema(description = "提问人")
    private String askerName;

    @Schema(description = "提问人电话")
    private String askerPhone;

    @Schema(description = "提问人邮箱")
    private String askerEmail;

    @Schema(description = "回答人ID")
    private Long answerUserId;

    @Schema(description = "回答时间")
    private LocalDateTime answerTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}