package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.KnowledgeCategoryEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 知识库更新
 */
@Data
public class KnowledgeUpdateForm {

    @Schema(description = "知识ID")
    @NotNull(message = "知识ID不能为空")
    private Long knowledgeId;

    @Schema(description = "标题")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @SchemaEnum(KnowledgeCategoryEnum.class)
    @CheckEnum(value = KnowledgeCategoryEnum.class, required = false, message = "分类错误")
    private Integer category;

    @Schema(description = "内容（富文本）")
    private String content;

    @Schema(description = "排序")
    private Integer sort;
}