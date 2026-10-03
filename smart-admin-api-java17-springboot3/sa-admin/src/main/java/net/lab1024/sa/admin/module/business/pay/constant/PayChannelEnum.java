package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 支付渠道
 */
@AllArgsConstructor
@Getter
public enum PayChannelEnum implements BaseEnum {

    WECHAT(1, "微信支付"),

    ALIPAY(2, "支付宝"),
    ;

    private final Integer value;

    private final String desc;
}
