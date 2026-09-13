package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 对账批次状态
 */
@AllArgsConstructor
@Getter
public enum PayReconBatchStatusEnum implements BaseEnum {

    RUNNING(10, "处理中"),

    DONE(20, "已完成"),

    FAIL(30, "失败"),
    ;

    private final Integer value;

    private final String desc;
}
