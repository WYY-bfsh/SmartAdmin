package net.lab1024.sa.admin.module.business.pay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.pay.service.PayOrderService;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付宝收银台：返回需要浏览器自动提交的 HTML 表单。
 * 前端用法：window.location.href = '/api/pay/alipay/wap/{payOrderId}'
 */
@RestController
@Tag(name = "支付宝收银台")
public class AlipayCashierController {

    @Resource
    private PayOrderService payOrderService;

    @Operation(summary = "手机网站支付收银台")
    @NoNeedLogin
    @GetMapping(value = "/pay/alipay/wap/{payOrderId}", produces = MediaType.TEXT_HTML_VALUE)
    public String wap(@PathVariable Long payOrderId) {
        return payOrderService.alipayWapForm(payOrderId);
    }

    @Operation(summary = "电脑网站支付收银台")
    @NoNeedLogin
    @GetMapping(value = "/pay/alipay/page/{payOrderId}", produces = MediaType.TEXT_HTML_VALUE)
    public String page(@PathVariable Long payOrderId) {
        return payOrderService.alipayPageForm(payOrderId);
    }
}
