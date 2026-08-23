package net.lab1024.sa.admin.module.business.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AiTaskVO {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "成片方式")
    private String type;

    @Schema(description = "进度")
    private Integer progress;

    @Schema(description = "状态")
    private Integer status;
}
