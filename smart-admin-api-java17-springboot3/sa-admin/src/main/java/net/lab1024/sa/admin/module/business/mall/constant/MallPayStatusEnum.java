package net.lab1024.sa.admin.module.business.mall.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 订单支付状态：10 待用户付款 / 15 已交凭证待商家确认 / 20 已确认收款 / 30 关闭。
 */
@AllArgsConstructor
@Getter
public enum MallPayStatusEnum implements BaseEnum {

    WAIT_PAY(10, "待支付"),
    WAIT_CONFIRM(15, "待确认"),
    PAID(20, "已支付"),
    CLOSED(30, "已关闭"),
    ;

    private final Integer value;
    private final String desc;
}
