package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class MallMemberUpdateForm {

    @NotNull(message = "会员不能为空")
    private Long memberId;

    @Schema(description = "昵称")
    @Length(max = 64)
    private String nickname;

    @Schema(description = "头像")
    @Length(max = 512)
    private String avatar;

    @Schema(description = "微信支付码")
    @Length(max = 512)
    private String wechatPayQr;

    @Schema(description = "支付宝收款码")
    @Length(max = 512)
    private String wechatReceiveQr;
}
