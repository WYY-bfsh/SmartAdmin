package net.lab1024.sa.admin.module.business.customer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 工单状态
 */
@AllArgsConstructor
@Getter
public enum TicketStatusEnum implements BaseEnum {

    WAIT_HANDLE(10, "待处理"),

    HANDLING(20, "处理中"),

    REPLIED(30, "已回复"),

    CLOSED(40, "已关闭"),
    ;

    private final Integer value;

    private final String desc;
}