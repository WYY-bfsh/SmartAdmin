package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_mall_order")
public class MallOrderEntity {

    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long memberId;

    private Long activityId;

    private String goodsName;

    private String coverUrl;

    private Integer qty;

    private BigDecimal price;

    private BigDecimal amount;

    private Integer payStatus;

    private Integer orderStatus;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String expressCode;

    private String expressName;

    private String waybillNo;

    private LocalDateTime shipTime;

    private LocalDateTime receiveTime;

    private LocalDateTime payTime;

    private LocalDateTime closeTime;

    private LocalDateTime expireTime;

    /**
     * 下单时锁定的上级，用于分销
     */
    private Long parentMemberId;

    private String remark;

    /**
     * 用户上传的付款截图，待付款提交后写入。
     */
    private String payProofUrl;

    /**
     * 用户填写的付款说明。
     */
    private String payNote;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
