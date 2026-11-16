package net.lab1024.sa.admin.module.business.customer.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送 AI 问答
 */
@Data
public class AiChatSendForm {

    @Size(max = 64, message = "话题编号过长")
    private String topicId;

    @NotBlank(message = "请输入问题")
    @Size(max = 2000, message = "问题不能超过 2000 字")
    private String message;

    /** 是否带上同一话题的最近几轮 */
    private Boolean useContext;
}
