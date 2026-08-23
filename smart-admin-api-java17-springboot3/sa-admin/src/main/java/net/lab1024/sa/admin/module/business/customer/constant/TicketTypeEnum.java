package net.lab1024.sa.admin.module.business.customer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 工单类型
 */
@AllArgsConstructor
@Getter
public enum TicketTypeEnum implements BaseEnum {

    QUESTION(1, "问题咨询"),

    SUGGESTION(2, "功能建议"),

    BUG(3, "Bug反馈"),

    BIZ_QUERY(4, "业务查询"),

    OTHER(5, "其他"),
    ;

    private final Integer value;

    private final String desc;
}