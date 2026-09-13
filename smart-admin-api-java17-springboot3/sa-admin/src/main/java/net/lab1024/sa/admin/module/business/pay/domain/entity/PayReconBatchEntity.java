package net.lab1024.sa.admin.module.business.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付对账批次
 */
@Data
@TableName("t_pay_recon_batch")
public class PayReconBatchEntity {

    @TableId(type = IdType.AUTO)
    private Long batchId;

    private LocalDate billDate;

    private Integer payChannel;

    private Integer sourceType;

    private Integer batchStatus;

    private Integer localCount;

    private Integer channelCount;

    private Integer matchedCount;

    private Integer amountDiffCount;

    private Integer statusDiffCount;

    private Integer localOnlyCount;

    private Integer channelOnlyCount;

    private Integer localAmount;

    private Integer channelAmount;

    private String fileName;

    private String errorMsg;

    private String remark;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
