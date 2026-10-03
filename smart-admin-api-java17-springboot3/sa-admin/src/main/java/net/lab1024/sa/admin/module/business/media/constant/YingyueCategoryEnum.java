package net.lab1024.sa.admin.module.business.media.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum YingyueCategoryEnum implements BaseEnum {
    MOVIE(1, "电影"),
    SERIES(2, "电视剧"),
    VARIETY(3, "综艺"),
    ANIME(4, "动漫"),
    DOC(5, "纪录片");

    private final Integer value;
    private final String desc;
}
