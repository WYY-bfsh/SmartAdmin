package net.lab1024.sa.admin.module.business.mall.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum MallPayStatusEnum implements BaseEnum {

    WAIT_PAY(10, "待支付"),
    PAID(20, "已支付"),
    CLOSED(30, "已关闭"),
    ;

    private final Integer value;
    private final String desc;
}
