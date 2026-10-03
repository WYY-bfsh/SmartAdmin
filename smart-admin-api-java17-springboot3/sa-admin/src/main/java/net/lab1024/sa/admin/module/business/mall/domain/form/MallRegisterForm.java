package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "H5 注册。邀请码必填。")
public class MallRegisterForm {

    @NotBlank(message = "请输入手机号")
    @Length(min = 11, max = 11, message = "请输入11位手机号")
    @Schema(description = "手机号，作为登录账号")
    private String phone;

    @NotBlank(message = "请输入密码")
    @Length(min = 6, max = 32, message = "密码至少6位")
    @Schema(description = "登录密码，注册时设置")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @NotBlank(message = "请输入邀请码")
    @Schema(description = "邀请码，必须对应已有会员")
    private String inviteCode;

    @Schema(description = "头像地址")
    @Length(max = 512, message = "头像地址过长")
    private String avatar;

    @Schema(description = "微信支付码")
    @Length(max = 512, message = "微信支付码地址过长")
    private String wechatPayQr;

    @Schema(description = "支付宝收款码")
    @Length(max = 512, message = "支付宝收款码地址过长")
    private String wechatReceiveQr;
}
