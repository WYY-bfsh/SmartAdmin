package net.lab1024.sa.admin.module.business.media.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum AiClipMaterialTypeEnum implements BaseEnum {
    VIDEO(1, "视频"),
    IMAGE(2, "图片"),
    AUDIO(3, "音频"),
    FONT(4, "字体字幕");

    private final Integer value;
    private final String desc;
}
