package net.lab1024.sa.admin.module.business.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PlaylistVO {

    @Schema(description = "歌单ID")
    private Long playlistId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "播放量")
    private String playCount;

    @Schema(description = "封面")
    private String coverUrl;
}
