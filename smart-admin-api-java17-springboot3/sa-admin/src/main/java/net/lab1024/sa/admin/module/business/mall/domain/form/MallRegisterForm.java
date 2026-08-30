package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class MallRegisterForm {

    @NotBlank(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "请输入11位手机号")
    private String phone;

    @NotBlank(message = "请输入密码")
    @Length(min = 6, max = 32, message = "密码至少6位")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邀请码")
    private String inviteCode;
}
