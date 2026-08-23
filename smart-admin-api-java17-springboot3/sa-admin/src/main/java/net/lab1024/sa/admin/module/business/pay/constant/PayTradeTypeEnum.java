package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 支付方式
 */
@AllArgsConstructor
@Getter
public enum PayTradeTypeEnum implements BaseEnum {

    NATIVE(1, "扫码支付"),
    ;

    private final Integer value;

    private final String desc;
}
