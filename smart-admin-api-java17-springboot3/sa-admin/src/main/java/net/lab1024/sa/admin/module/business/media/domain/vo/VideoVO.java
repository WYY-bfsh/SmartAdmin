package net.lab1024.sa.admin.module.business.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class VideoVO {

    @Schema(description = "影片ID")
    private Long videoId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "播放地址")
    private String playUrl;

    @Schema(description = "封面")
    private String coverUrl;
}
