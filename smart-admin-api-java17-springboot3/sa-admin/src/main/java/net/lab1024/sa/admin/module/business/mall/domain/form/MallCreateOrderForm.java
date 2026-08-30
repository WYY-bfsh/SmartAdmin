package net.lab1024.sa.admin.module.business.mall.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MallCreateOrderForm {

    @NotNull(message = "请选择秒杀活动")
    private Long activityId;

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    @NotNull
    @Min(value = 1, message = "数量至少为1")
    private Integer qty = 1;
}
