package net.lab1024.sa.admin.module.business.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AiProjectVO {

    @Schema(description = "作品ID")
    private Long projectId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "封面")
    private String coverUrl;
}
