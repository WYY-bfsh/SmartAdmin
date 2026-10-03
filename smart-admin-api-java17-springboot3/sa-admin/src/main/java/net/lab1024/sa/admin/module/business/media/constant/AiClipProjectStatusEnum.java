package net.lab1024.sa.admin.module.business.media.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum AiClipProjectStatusEnum implements BaseEnum {
    DRAFT(10, "草稿"),
    RENDERING(20, "成片中"),
    DONE(30, "已成片"),
    FAILED(40, "失败");

    private final Integer value;
    private final String desc;
}
