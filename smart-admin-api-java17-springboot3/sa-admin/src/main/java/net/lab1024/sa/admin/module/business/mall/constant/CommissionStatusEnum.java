package net.lab1024.sa.admin.module.business.mall.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum CommissionStatusEnum implements BaseEnum {

    FROZEN(10, "待结算"),
    SETTLED(20, "已结算"),
    CANCELED(30, "已取消"),
    ;

    private final Integer value;
    private final String desc;
}
