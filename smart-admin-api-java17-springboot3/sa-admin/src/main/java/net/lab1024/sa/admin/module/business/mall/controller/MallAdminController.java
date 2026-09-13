package net.lab1024.sa.admin.module.business.mall.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallCommissionQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallMemberQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallMemberUpdateForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallOrderQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallRejectPayForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallSettingForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallShipForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.SeckillActivityForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.SeckillActivityQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.ExpressCompanyVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallCommissionVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallConfigVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallMemberVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallOrderVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.SeckillActivityVO;
import net.lab1024.sa.admin.module.business.mall.service.MallExpressService;
import net.lab1024.sa.admin.module.business.mall.service.MallMemberService;
import net.lab1024.sa.admin.module.business.mall.service.MallOrderService;
import net.lab1024.sa.admin.module.business.mall.service.MallAlipayPayService;
import net.lab1024.sa.admin.module.business.mall.service.MallWechatPayService;
import net.lab1024.sa.admin.module.business.mall.service.SeckillActivityService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.operatelog.annotation.OperateLog;
import net.lab1024.sa.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@OperateLog
@RestController
@Tag(name = AdminSwaggerTagConst.Business.MALL_SECKILL, description = "管理端：活动、商家收款码、确认收款、发货、会员、佣金")
public class MallAdminController {

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private MallOrderService mallOrderService;

    @Resource
    private MallWechatPayService mallWechatPayService;

    @Resource
    private MallAlipayPayService mallAlipayPayService;

    @Resource
    private MallMemberService mallMemberService;

    @Resource
    private MallExpressService mallExpressService;

    @Operation(summary = "秒杀配置", description = "同时返回商家收款码，供活动页展示/编辑")
    @GetMapping("/mall/admin/config")
    @SaCheckPermission("mall:activity:query")
    public ResponseDTO<MallConfigVO> config() {
        return ResponseDTO.ok(seckillActivityService.config());
    }

    @Operation(summary = "分页查询秒杀活动")
    @PostMapping("/mall/admin/activity/query")
    @SaCheckPermission("mall:activity:query")
    public ResponseDTO<PageResult<SeckillActivityVO>> activityQuery(@RequestBody @Valid SeckillActivityQueryForm queryForm) {
        return seckillActivityService.query(queryForm);
    }

    @Operation(summary = "新建或编辑秒杀活动")
    @PostMapping("/mall/admin/activity/save")
    @SaCheckPermission("mall:activity:save")
    @RepeatSubmit
    public ResponseDTO<String> activitySave(@RequestBody @Valid SeckillActivityForm form) {
        return seckillActivityService.save(form);
    }

    @Operation(summary = "保存商家收款码", description = "待付款页展示的微信/支付宝码，与会员分销收款码无关")
    @PostMapping("/mall/admin/setting/save")
    @SaCheckPermission("mall:activity:save")
    @RepeatSubmit
    public ResponseDTO<String> saveSetting(@RequestBody @Valid MallSettingForm form) {
        return seckillActivityService.saveSetting(form);
    }

    @Operation(summary = "确认收款", description = "待商家确认(15)→待发货(20)，并冻结分销佣金")
    @PostMapping("/mall/admin/order/confirm-pay/{orderId}")
    @SaCheckPermission("mall:order:ship")
    @RepeatSubmit
    public ResponseDTO<String> confirmPay(@PathVariable Long orderId) {
        return mallOrderService.confirmPay(orderId);
    }

    @Operation(summary = "拒绝收款", description = "待商家确认(15)→已关闭(50)，回库存。截图无效或未到账时使用")
    @PostMapping("/mall/admin/order/reject-pay")
    @SaCheckPermission("mall:order:ship")
    @RepeatSubmit
    public ResponseDTO<String> rejectPay(@RequestBody @Valid MallRejectPayForm form) {
        return mallOrderService.rejectPay(form);
    }

    @Operation(summary = "分页查询订单")
    @PostMapping("/mall/admin/order/query")
    @SaCheckPermission("mall:order:query")
    public ResponseDTO<PageResult<MallOrderVO>> orderQuery(@RequestBody @Valid MallOrderQueryForm queryForm) {
        return mallOrderService.queryAdmin(queryForm);
    }

    @Operation(summary = "订单详情", description = "含付款截图、付款说明，供确认收款核对")
    @GetMapping("/mall/admin/order/{orderId}")
    @SaCheckPermission("mall:order:query")
    public ResponseDTO<MallOrderVO> orderDetail(@PathVariable Long orderId) {
        return mallOrderService.detail(orderId, true);
    }

    @Operation(summary = "微信退款并关单", description = "已微信支付的待发货/已发货订单退款后关单回库存")
    @PostMapping("/mall/admin/order/wechat-refund/{orderId}")
    @SaCheckPermission("mall:order:ship")
    @RepeatSubmit
    public ResponseDTO<String> wechatRefund(@PathVariable Long orderId, @RequestBody(required = false) MallRejectPayForm form) {
        String reason = form == null ? null : form.getRemark();
        return mallWechatPayService.refundClose(orderId, reason);
    }

    @Operation(summary = "支付宝退款并关单")
    @PostMapping("/mall/admin/order/alipay-refund/{orderId}")
    @SaCheckPermission("mall:order:ship")
    @RepeatSubmit
    public ResponseDTO<String> alipayRefund(@PathVariable Long orderId, @RequestBody(required = false) MallRejectPayForm form) {
        String reason = form == null ? null : form.getRemark();
        return mallAlipayPayService.refundClose(orderId, reason);
    }

    @Operation(summary = "发货")
    @PostMapping("/mall/admin/order/ship")
    @SaCheckPermission("mall:order:ship")
    @RepeatSubmit
    public ResponseDTO<String> ship(@RequestBody @Valid MallShipForm form) {
        return mallOrderService.ship(form);
    }

    @Operation(summary = "快递公司列表")
    @GetMapping("/mall/admin/express/companies")
    @SaCheckPermission("mall:order:query")
    public ResponseDTO<List<ExpressCompanyVO>> companies() {
        return ResponseDTO.ok(mallExpressService.companies());
    }

    @Operation(summary = "分页查询会员")
    @PostMapping("/mall/admin/member/query")
    @SaCheckPermission("mall:member:query")
    public ResponseDTO<PageResult<MallMemberVO>> memberQuery(@RequestBody @Valid MallMemberQueryForm queryForm) {
        return mallMemberService.queryMembers(queryForm);
    }

    @Operation(summary = "更新会员资料", description = "头像与分销用收款码")
    @PostMapping("/mall/admin/member/update")
    @SaCheckPermission("mall:member:query")
    @RepeatSubmit
    public ResponseDTO<String> memberUpdate(@RequestBody @Valid MallMemberUpdateForm form) {
        return mallMemberService.updateMember(form);
    }

    @Operation(summary = "分销佣金列表")
    @PostMapping("/mall/admin/commission/query")
    @SaCheckPermission("mall:commission:query")
    public ResponseDTO<PageResult<MallCommissionVO>> commissionQuery(@RequestBody @Valid MallCommissionQueryForm queryForm) {
        return mallOrderService.queryCommission(queryForm, false);
    }
}
