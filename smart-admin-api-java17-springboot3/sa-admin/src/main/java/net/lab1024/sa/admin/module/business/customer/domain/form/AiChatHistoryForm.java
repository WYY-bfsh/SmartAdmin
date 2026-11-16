package net.lab1024.sa.admin.module.business.customer.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存问答界面
 */
@Data
public class AiChatHistoryForm {

    @NotNull(message = "界面内容不能为空")
    private String content;
}
