package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 对账业务类型
 */
@AllArgsConstructor
@Getter
public enum PayReconBizTypeEnum implements BaseEnum {

    TRADE(1, "交易"),

    REFUND(2, "退款"),
    ;

    private final Integer value;

    private final String desc;
}
