package net.lab1024.sa.admin.module.business.media.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum MusicPlayModeEnum implements BaseEnum {
    SEQUENCE(1, "顺序播放"),
    LOOP(2, "单曲循环"),
    RANDOM(3, "随机播放");

    private final Integer value;
    private final String desc;
}
