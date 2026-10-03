package net.lab1024.sa.admin.module.business.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付对账明细
 */
@Data
@TableName("t_pay_recon_item")
public class PayReconItemEntity {

    @TableId(type = IdType.AUTO)
    private Long itemId;

    private Long batchId;

    private Integer matchStatus;

    private Integer bizType;

    private Long payOrderId;

    private String orderNo;

    private Integer localAmount;

    private Integer localStatus;

    private String channelTradeNo;

    private String channelOrderNo;

    private Integer channelAmount;

    private String channelStatus;

    private String channelTime;

    private Integer diffAmount;

    private Boolean handledFlag;

    private String remark;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
