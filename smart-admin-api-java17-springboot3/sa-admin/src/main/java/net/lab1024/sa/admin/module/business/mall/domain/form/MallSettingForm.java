package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "商家收款码（待付款页展示）")
public class MallSettingForm {

    @Length(max = 512)
    @Schema(description = "商家微信收款码图片地址")
    private String merchantWechatQr;

    @Length(max = 512)
    @Schema(description = "商家支付宝收款码图片地址")
    private String merchantAlipayQr;
}
