package net.lab1024.sa.admin.module.business.media.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

@Data
public class MediaQueryForm extends PageParam {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "状态/分类")
    private Integer status;

    @Schema(description = "素材类型")
    private Integer type;

    @Schema(description = "场景")
    private Integer scene;

    @Schema(description = "影片分类")
    private Integer category;

    @Schema(description = "地区")
    private String area;
}
