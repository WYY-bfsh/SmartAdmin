package net.lab1024.sa.admin.module.business.pay.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.pay.service.PayOrderService;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 微信支付结果通知，微信服务器调用，无需登录。
 */
@Slf4j
@Hidden
@RestController
public class WeChatPayNotifyController {

    @Resource
    private PayOrderService payOrderService;

    @NoNeedLogin
    @PostMapping("/pay/wechat/notify")
    public void notify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String result = payOrderService.handleNotify(request);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (result.contains("\"FAIL\"")) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        response.getWriter().write(result);
    }
}
