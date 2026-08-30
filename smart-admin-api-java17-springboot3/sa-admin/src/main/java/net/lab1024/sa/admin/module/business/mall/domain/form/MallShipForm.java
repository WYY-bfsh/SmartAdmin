package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MallShipForm {

    @NotNull(message = "请选择订单")
    private Long orderId;

    @NotBlank(message = "请选择快递公司")
    @Schema(description = "快递100公司编码，如 shunfeng")
    private String expressCode;

    @NotBlank(message = "请填写运单号")
    private String waybillNo;
}
