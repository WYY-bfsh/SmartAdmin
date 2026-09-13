package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "提交付款截图与说明")
public class MallPayProofForm {

    @NotNull(message = "订单不能为空")
    @Schema(description = "订单 ID")
    private Long orderId;

    @NotBlank(message = "请上传付款截图")
    @Length(max = 512, message = "付款截图地址过长")
    @Schema(description = "付款截图 URL，先调 /mall/h5/avatar/upload")
    private String payProofUrl;

    @Length(max = 255, message = "说明最多255字")
    @Schema(description = "付款说明，选填")
    private String payNote;
}
