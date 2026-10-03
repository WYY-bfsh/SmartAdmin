package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 对账匹配结果
 */
@AllArgsConstructor
@Getter
public enum PayReconMatchStatusEnum implements BaseEnum {

    MATCHED(10, "完全匹配"),

    AMOUNT_DIFF(20, "金额不符"),

    STATUS_DIFF(30, "状态不符"),

    LOCAL_ONLY(40, "仅本地有"),

    CHANNEL_ONLY(50, "仅渠道有"),
    ;

    private final Integer value;

    private final String desc;
}
