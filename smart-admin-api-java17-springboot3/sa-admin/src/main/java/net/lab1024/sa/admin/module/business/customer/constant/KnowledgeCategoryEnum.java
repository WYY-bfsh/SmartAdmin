package net.lab1024.sa.admin.module.business.customer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 知识库分类
 */
@AllArgsConstructor
@Getter
public enum KnowledgeCategoryEnum implements BaseEnum {

    FAQ(1, "常见问题"),

    TUTORIAL(2, "使用教程"),

    BIZ_DESC(3, "业务说明"),

    OTHER(4, "其他"),
    ;

    private final Integer value;

    private final String desc;
}