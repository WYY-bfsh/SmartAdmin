package net.lab1024.sa.admin.module.business.mall.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 商城订单状态。
 * 10 待付款 → 15 待商家确认（用户已交截图）→ 20 待发货（后台确认收款）→ 30 已发货 → 40 已完成；50 已关闭。
 */
@AllArgsConstructor
@Getter
public enum MallOrderStatusEnum implements BaseEnum {

    WAIT_PAY(10, "待付款"),
    WAIT_CONFIRM(15, "待商家确认"),
    WAIT_SHIP(20, "待发货"),
    SHIPPED(30, "已发货"),
    COMPLETED(40, "已完成"),
    CLOSED(50, "已关闭"),
    ;

    private final Integer value;
    private final String desc;
}
