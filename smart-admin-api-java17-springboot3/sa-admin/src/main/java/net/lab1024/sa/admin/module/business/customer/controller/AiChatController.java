package net.lab1024.sa.admin.module.business.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.customer.domain.form.AiChatHistoryForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.AiChatSendForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.AiChatHistoryVO;
import net.lab1024.sa.admin.module.business.customer.service.AiChatService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 客服 AI 问答。流式输出，多轮上下文放在现有 Redis。
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.CUSTOMER_AI)
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    @Operation(summary = "发送问题并流式返回答案")
    @PostMapping(value = "/customer/ai/chat/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaCheckPermission("cs:ai:chat")
    public SseEmitter send(@RequestBody @Valid AiChatSendForm form) {
        return aiChatService.send(form);
    }

    @Operation(summary = "保存问答界面")
    @PostMapping("/customer/ai/chat/history/save")
    @SaCheckPermission("cs:ai:chat")
    public ResponseDTO<String> saveHistory(@RequestBody @Valid AiChatHistoryForm form) {
        return aiChatService.saveHistory(form);
    }

    @Operation(summary = "读取问答界面")
    @GetMapping("/customer/ai/chat/history/get")
    @SaCheckPermission("cs:ai:chat")
    public ResponseDTO<AiChatHistoryVO> getHistory() {
        return aiChatService.getHistory();
    }
}
