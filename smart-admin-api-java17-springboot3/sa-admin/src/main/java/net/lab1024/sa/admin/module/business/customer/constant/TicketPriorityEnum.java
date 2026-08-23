package net.lab1024.sa.admin.module.business.customer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 工单优先级
 */
@AllArgsConstructor
@Getter
public enum TicketPriorityEnum implements BaseEnum {

    LOW(1, "低"),

    MEDIUM(2, "中"),

    HIGH(3, "高"),

    URGENT(4, "紧急"),
    ;

    private final Integer value;

    private final String desc;
}