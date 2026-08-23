package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.KnowledgeCategoryEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 知识库分页查询
 */
@Data
public class KnowledgeQueryForm extends PageParam {

    @Schema(description = "标题")
    @Length(max = 200, message = "标题最多200个字符")
    private String title;

    @SchemaEnum(KnowledgeCategoryEnum.class)
    @CheckEnum(value = KnowledgeCategoryEnum.class, required = false, message = "分类错误")
    private Integer category;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}