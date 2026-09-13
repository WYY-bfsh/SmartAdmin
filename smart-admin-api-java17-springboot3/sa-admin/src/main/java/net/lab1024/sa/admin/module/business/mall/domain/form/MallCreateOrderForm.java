package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建秒杀订单，一单一种商品")
public class MallCreateOrderForm {

    @NotNull(message = "请选择秒杀活动")
    @Schema(description = "秒杀活动 ID")
    private Long activityId;

    @NotNull(message = "请选择收货地址")
    @Schema(description = "收货地址 ID")
    private Long addressId;

    @NotNull
    @Min(value = 1, message = "数量至少为1")
    @Schema(description = "购买数量，不超过每人限购与库存")
    private Integer qty = 1;
}
