package net.lab1024.sa.admin.module.business.pay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 对账账单来源
 */
@AllArgsConstructor
@Getter
public enum PayReconSourceEnum implements BaseEnum {

    PULL(1, "渠道拉取"),

    UPLOAD(2, "文件上传"),

    MOCK(3, "演示对账"),
    ;

    private final Integer value;

    private final String desc;
}
