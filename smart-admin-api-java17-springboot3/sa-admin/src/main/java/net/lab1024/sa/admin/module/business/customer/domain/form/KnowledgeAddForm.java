package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.KnowledgeCategoryEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 知识库新增
 */
@Data
public class KnowledgeAddForm {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @SchemaEnum(KnowledgeCategoryEnum.class)
    @CheckEnum(value = KnowledgeCategoryEnum.class, required = true, message = "分类错误")
    private Integer category;

    @Schema(description = "内容（富文本）")
    private String content;

    @Schema(description = "排序")
    private Integer sort;
}