package net.lab1024.sa.admin.module.business.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconBatchStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayReconSourceEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对账批次
 */
@Data
public class PayReconBatchVO {

    private Long batchId;

    private LocalDate billDate;

    @SchemaEnum(PayChannelEnum.class)
    private Integer payChannel;

    @SchemaEnum(PayReconSourceEnum.class)
    private Integer sourceType;

    @SchemaEnum(PayReconBatchStatusEnum.class)
    private Integer batchStatus;

    private Integer localCount;

    private Integer channelCount;

    private Integer matchedCount;

    private Integer amountDiffCount;

    private Integer statusDiffCount;

    private Integer localOnlyCount;

    private Integer channelOnlyCount;

    private Integer localAmount;

    @Schema(description = "本地金额（元）")
    private BigDecimal localAmountYuan;

    private Integer channelAmount;

    @Schema(description = "渠道金额（元）")
    private BigDecimal channelAmountYuan;

    private String fileName;

    private String errorMsg;

    private String remark;

    private LocalDateTime createTime;
}
