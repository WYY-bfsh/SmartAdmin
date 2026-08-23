package net.lab1024.sa.admin.module.business.customer.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.customer.constant.QAStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * 问答分页查询
 */
@Data
public class QAQueryForm extends PageParam {

    @Schema(description = "提问内容")
    @Length(max = 500, message = "搜索内容最多500个字符")
    private String question;

    @SchemaEnum(QAStatusEnum.class)
    @CheckEnum(value = QAStatusEnum.class, required = false, message = "状态错误")
    private Integer status;

    @Schema(description = "创建-开始日期")
    private LocalDate createTimeBegin;

    @Schema(description = "创建-截止日期")
    private LocalDate createTimeEnd;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}