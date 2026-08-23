package net.lab1024.sa.admin.module.business.customer.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.KnowledgeCategoryEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 知识库
 */
@Data
public class KnowledgeVO {

    @Schema(description = "知识ID")
    private Long knowledgeId;

    @Schema(description = "标题")
    private String title;

    @SchemaEnum(KnowledgeCategoryEnum.class)
    private Integer category;

    @Schema(description = "内容（富文本）")
    private String content;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}