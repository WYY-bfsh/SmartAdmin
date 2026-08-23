package net.lab1024.sa.admin.module.business.media.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AiProjectSaveForm {

    @Schema(description = "作品ID，空则新建")
    private Long projectId;

    @Schema(description = "标题")
    @Length(max = 128)
    private String title;

    @Schema(description = "场景")
    private Integer scene;
}
