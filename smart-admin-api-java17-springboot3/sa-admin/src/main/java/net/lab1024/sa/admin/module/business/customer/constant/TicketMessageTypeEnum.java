package net.lab1024.sa.admin.module.business.customer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 工单消息类型
 */
@AllArgsConstructor
@Getter
public enum TicketMessageTypeEnum implements BaseEnum {

    CUSTOMER(1, "客户消息"),

    SERVICE(2, "客服回复"),

    SYSTEM(3, "系统消息"),
    ;

    private final Integer value;

    private final String desc;
}