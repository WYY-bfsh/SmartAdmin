package net.lab1024.sa.admin.module.business.customer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 问答状态
 */
@AllArgsConstructor
@Getter
public enum QAStatusEnum implements BaseEnum {

    WAIT_ANSWER(10, "待回答"),

    ANSWERED(20, "已回答"),

    REJECTED(30, "已驳回"),
    ;

    private final Integer value;

    private final String desc;
}