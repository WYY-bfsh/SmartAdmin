package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单回复
 */
@Data
public class TicketReplyForm {

    @Schema(description = "工单ID")
    @NotNull(message = "工单ID不能为空")
    private Long ticketId;

    @Schema(description = "回复内容")
    @NotBlank(message = "回复内容不能为空")
    private String content;
}