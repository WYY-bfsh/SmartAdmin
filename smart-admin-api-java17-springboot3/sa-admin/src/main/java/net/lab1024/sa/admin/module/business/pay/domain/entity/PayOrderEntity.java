package net.lab1024.sa.admin.module.business.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信支付订单
 */
@Data
@TableName("t_pay_order")
public class PayOrderEntity {

    @TableId(type = IdType.AUTO)
    private Long payOrderId;

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 关联秒杀订单，独立后台收款可为空
     */
    private Long mallOrderId;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 订单金额，单位分
     */
    private Integer amount;

    /**
     * 支付渠道 1微信 2支付宝
     */
    private Integer payChannel;

    /**
     * 支付方式
     */
    private Integer tradeType;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * Native 支付二维码内容
     */
    private String codeUrl;

    /**
     * 微信支付订单号
     */
    private String transactionId;

    /**
     * 用户标识
     */
    private String openid;

    /**
     * 用户实付金额，单位分
     */
    private Integer payerTotal;

    /**
     * 支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 商户退款单号
     */
    private String refundNo;

    /**
     * 微信退款单号
     */
    private String refundId;

    /**
     * 已退款金额，单位分
     */
    private Integer refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 回调原文
     */
    private String notifyContent;

    /**
     * 备注
     */
    private String remark;

    private Long createUserId;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
