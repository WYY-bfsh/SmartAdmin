package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 支付订单状态
 */
@AllArgsConstructor
@Getter
public enum PayStatusEnum implements BaseEnum {

    WAIT_PAY(10, "待支付"),

    SUCCESS(20, "支付成功"),

    CLOSED(30, "已关闭"),

    REFUNDING(40, "退款中"),

    REFUND(50, "已退款"),
    ;

    private final Integer value;

    private final String desc;
}
