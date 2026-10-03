package net.lab1024.sa.admin.module.business.pay.service;

import lombok.Data;

/**
 * 渠道账单一行
 */
@Data
public class PayBillLine {

    /**
     * 1交易 2退款
     */
    private Integer bizType;

    private String orderNo;

    private String tradeNo;

    private String refundNo;

    /**
     * 金额，分（退款为正数）
     */
    private Integer amountFen;

    private String channelStatus;

    private String channelTime;
}
