package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增提问
 */
@Data
public class QAAddForm {

    @Schema(description = "提问内容")
    @NotBlank(message = "提问内容不能为空")
    @Size(max = 500, message = "提问内容最多500个字符")
    private String question;

    @Schema(description = "提问人")
    @Size(max = 50, message = "提问人最多50个字符")
    private String askerName;

    @Schema(description = "提问人电话")
    @Size(max = 20, message = "电话最多20个字符")
    private String askerPhone;

    @Schema(description = "提问人邮箱")
    @Size(max = 100, message = "邮箱最多100个字符")
    private String askerEmail;
}