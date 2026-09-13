package net.lab1024.sa.admin.module.business.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商城配置")
public class MallConfigVO {

    @Schema(description = "同时抢购人数上限")
    private Integer concurrentLimit;
    @Schema(description = "待付款超时分钟数，仅关闭未交凭证的订单")
    private Integer payTimeoutMinutes;
    @Schema(description = "一级分销默认比例")
    private BigDecimal defaultCommissionRate;
    @Schema(description = "二级分销默认比例")
    private BigDecimal defaultCommissionRateL2;
    @Schema(description = "是否已配置快递100")
    private Boolean kuaidi100Enabled;
    @Schema(description = "H5 访问地址")
    private String h5Path;
    @Schema(description = "商家微信收款码")
    private String merchantWechatQr;
    @Schema(description = "商家支付宝收款码")
    private String merchantAlipayQr;
}
