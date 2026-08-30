package net.lab1024.sa.admin.module.business.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderCreateForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayRefundForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayCreateVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayOrderVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.WeChatPayConfigVO;
import net.lab1024.sa.admin.module.business.pay.service.PayOrderService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.operatelog.annotation.OperateLog;
import net.lab1024.sa.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付订单
 */
@OperateLog
@RestController
@Tag(name = AdminSwaggerTagConst.Business.PAY_WECHAT)
public class PayOrderController {

    @Resource
    private PayOrderService payOrderService;

    @Operation(summary = "微信支付配置概览")
    @GetMapping("/pay/wechat/config")
    @SaCheckPermission("pay:config:query")
    public ResponseDTO<WeChatPayConfigVO> config() {
        return payOrderService.getConfig();
    }

    @Operation(summary = "分页查询支付订单")
    @PostMapping("/pay/order/query")
    @SaCheckPermission("pay:order:query")
    public ResponseDTO<PageResult<PayOrderVO>> query(@RequestBody @Valid PayOrderQueryForm queryForm) {
        return payOrderService.query(queryForm);
    }

    @Operation(summary = "支付订单详情")
    @GetMapping("/pay/order/detail/{payOrderId}")
    @SaCheckPermission("pay:order:query")
    public ResponseDTO<PayOrderVO> detail(@PathVariable Long payOrderId) {
        return payOrderService.detail(payOrderId);
    }

    @Operation(summary = "发起扫码支付")
    @PostMapping("/pay/order/create")
    @SaCheckPermission("pay:order:create")
    @RepeatSubmit
    public ResponseDTO<PayCreateVO> create(@RequestBody @Valid PayOrderCreateForm createForm) {
        return payOrderService.create(createForm);
    }

    @Operation(summary = "获取支付二维码")
    @GetMapping("/pay/order/qrcode/{payOrderId}")
    @SaCheckPermission("pay:order:query")
    public ResponseDTO<PayCreateVO> qrcode(@PathVariable Long payOrderId) {
        return payOrderService.qrcode(payOrderId);
    }

    @Operation(summary = "演示模式模拟支付成功")
    @PostMapping("/pay/order/mock-pay/{payOrderId}")
    @SaCheckPermission("pay:order:sync")
    @RepeatSubmit
    public ResponseDTO<PayOrderVO> mockPay(@PathVariable Long payOrderId) {
        return payOrderService.mockPay(payOrderId);
    }

    @Operation(summary = "同步微信支付状态")
    @GetMapping("/pay/order/sync/{payOrderId}")
    @SaCheckPermission("pay:order:sync")
    public ResponseDTO<PayOrderVO> sync(@PathVariable Long payOrderId) {
        return payOrderService.sync(payOrderId);
    }

    @Operation(summary = "关闭支付订单")
    @GetMapping("/pay/order/close/{payOrderId}")
    @SaCheckPermission("pay:order:close")
    @RepeatSubmit
    public ResponseDTO<String> close(@PathVariable Long payOrderId) {
        return payOrderService.close(payOrderId);
    }

    @Operation(summary = "申请退款")
    @PostMapping("/pay/order/refund")
    @SaCheckPermission("pay:order:refund")
    @RepeatSubmit
    public ResponseDTO<String> refund(@RequestBody @Valid PayRefundForm refundForm) {
        return payOrderService.refund(refundForm);
    }
}
