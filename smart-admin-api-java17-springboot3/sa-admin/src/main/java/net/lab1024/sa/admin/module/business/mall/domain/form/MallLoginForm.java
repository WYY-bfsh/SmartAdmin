package net.lab1024.sa.admin.module.business.mall.domain.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MallLoginForm {

    @NotBlank(message = "请输入手机号")
    private String phone;

    @NotBlank(message = "请输入密码")
    private String password;
}
