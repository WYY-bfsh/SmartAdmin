package net.lab1024.sa.admin.module.business.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SongVO {

    @Schema(description = "歌曲ID")
    private Long songId;

    @Schema(description = "歌名")
    private String name;

    @Schema(description = "歌手")
    private String artist;

    @Schema(description = "播放地址")
    private String audioUrl;
}
