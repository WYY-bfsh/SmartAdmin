package net.lab1024.sa.admin.module.business.mall.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MallAddressForm {

    private Long addressId;

    @NotBlank(message = "请填写收货人")
    private String receiverName;

    @NotBlank(message = "请填写手机号")
    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    @NotBlank(message = "请填写详细地址")
    private String detail;

    @NotNull
    private Boolean defaultFlag = Boolean.TRUE;
}
