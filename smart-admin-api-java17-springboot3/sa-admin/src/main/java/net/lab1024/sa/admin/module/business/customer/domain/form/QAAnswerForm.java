package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 回答/驳回
 */
@Data
public class QAAnswerForm {

    @Schema(description = "问答ID")
    @NotNull(message = "问答ID不能为空")
    private Long qaId;

    @Schema(description = "回答内容（驳回时可为空）")
    private String answer;

    @Schema(description = "操作：answer回答 reject驳回")
    @NotBlank(message = "操作类型不能为空")
    private String action;
}