package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "拒绝用户付款凭证，关单并回库存")
public class MallRejectPayForm {

    @NotNull(message = "订单不能为空")
    @Schema(description = "订单 ID")
    private Long orderId;

    @Length(max = 255)
    @Schema(description = "拒绝原因，写入订单备注")
    private String remark;
}
