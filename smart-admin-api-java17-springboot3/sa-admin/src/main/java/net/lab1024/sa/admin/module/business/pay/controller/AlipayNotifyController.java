package net.lab1024.sa.admin.module.business.pay.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.pay.service.PayOrderService;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝异步通知。由支付宝服务器调用，无需登录。
 * 验签通过必须返回纯文本 success，否则支付宝会持续重推。
 */
@Slf4j
@Hidden
@RestController
public class AlipayNotifyController {

    @Resource
    private PayOrderService payOrderService;

    @NoNeedLogin
    @PostMapping(value = "/pay/alipay/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            params.put(entry.getKey(), values == null || values.length == 0 ? "" : values[0]);
        }
        try {
            if (payOrderService.handleAlipayNotify(params)) {
                return "success";
            }
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
        }
        return "failure";
    }
}
