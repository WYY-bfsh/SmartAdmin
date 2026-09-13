package net.lab1024.sa.admin.module.business.pay.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 人工核销差异
 */
@Data
public class PayReconHandleForm {

    @NotNull(message = "明细不能为空")
    @Schema(description = "明细ID")
    private Long itemId;

    @Schema(description = "核销说明")
    @Length(max = 500)
    private String remark;
}
