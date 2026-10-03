package net.lab1024.sa.admin.module.business.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconBizTypeEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconMatchStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账明细
 */
@Data
public class PayReconItemVO {

    private Long itemId;

    private Long batchId;

    @SchemaEnum(PayReconMatchStatusEnum.class)
    private Integer matchStatus;

    @SchemaEnum(PayReconBizTypeEnum.class)
    private Integer bizType;

    private Long payOrderId;

    private String orderNo;

    private Integer localAmount;

    @Schema(description = "本地金额（元）")
    private BigDecimal localAmountYuan;

    @SchemaEnum(PayStatusEnum.class)
    private Integer localStatus;

    private String channelTradeNo;

    private String channelOrderNo;

    private Integer channelAmount;

    @Schema(description = "渠道金额（元）")
    private BigDecimal channelAmountYuan;

    private String channelStatus;

    private String channelTime;

    private Integer diffAmount;

    @Schema(description = "差额（元）")
    private BigDecimal diffAmountYuan;

    private Boolean handledFlag;

    private String remark;

    private LocalDateTime createTime;
}
