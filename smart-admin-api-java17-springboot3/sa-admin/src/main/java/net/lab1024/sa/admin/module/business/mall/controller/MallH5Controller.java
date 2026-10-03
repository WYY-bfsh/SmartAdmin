package net.lab1024.sa.admin.module.business.mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallMemberEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallAddressForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallCommissionQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallCreateOrderForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallLoginForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallPayProofForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallRegisterForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallWechatPrepayForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallAlipayPayVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallWechatPayVO;
import net.lab1024.sa.admin.module.business.mall.service.MallAlipayPayService;
import net.lab1024.sa.admin.module.business.mall.service.MallWechatPayService;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallAddressVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallCommissionVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallConfigVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallMemberVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallOrderVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.SeckillActivityVO;
import net.lab1024.sa.admin.module.business.mall.service.MallMemberService;
import net.lab1024.sa.admin.module.business.mall.service.MallOrderService;
import net.lab1024.sa.admin.module.business.mall.service.SeckillActivityService;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.support.file.domain.vo.FileUploadVO;
import net.lab1024.sa.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = AdminSwaggerTagConst.Business.MALL_H5, description = "用户端 H5：注册登录、秒杀、下单与付款凭证")
public class MallH5Controller {

    @Resource
    private MallMemberService mallMemberService;

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private MallOrderService mallOrderService;

    @Resource
    private MallWechatPayService mallWechatPayService;

    @Resource
    private MallAlipayPayService mallAlipayPayService;

    @NoNeedLogin
    @Operation(summary = "商城配置", description = "含支付超时、同时抢购人数、商家微信/支付宝收款码（待付款页展示）")
    @GetMapping("/mall/h5/config")
    public ResponseDTO<MallConfigVO> config() {
        return ResponseDTO.ok(seckillActivityService.config());
    }

    @NoNeedLogin
    @Operation(summary = "上传图片", description = "头像、会员收款码、付款截图共用")
    @PostMapping("/mall/h5/avatar/upload")
    public ResponseDTO<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return mallMemberService.uploadAvatar(file);
    }

    @NoNeedLogin
    @Operation(summary = "手机号注册", description = "邀请码必填且须为已有会员邀请码；注册时设置密码；短信验证码暂未启用")
    @PostMapping("/mall/h5/register")
    public ResponseDTO<MallMemberVO> register(@RequestBody @Valid MallRegisterForm form) {
        return mallMemberService.register(form);
    }

    @NoNeedLogin
    @Operation(summary = "账号密码登录", description = "账号为 11 位手机号")
    @PostMapping("/mall/h5/login")
    public ResponseDTO<MallMemberVO> login(@RequestBody @Valid MallLoginForm form) {
        return mallMemberService.login(form);
    }

    @NoNeedLogin
    @Operation(summary = "当前会员信息")
    @GetMapping("/mall/h5/me")
    public ResponseDTO<MallMemberVO> me() {
        return mallMemberService.me();
    }

    @NoNeedLogin
    @Operation(summary = "秒杀活动列表", description = "H5 倒计时按未结束场次的最早开始时间计算；开售前 30 分钟可预览")
    @GetMapping("/mall/h5/activity/list")
    public ResponseDTO<List<SeckillActivityVO>> activityList() {
        return seckillActivityService.listForH5();
    }

    @NoNeedLogin
    @Operation(summary = "秒杀活动详情", description = "saleStatus：10未开始仅预览，20进行中可下单，30已结束/售罄")
    @GetMapping("/mall/h5/activity/{activityId}")
    public ResponseDTO<SeckillActivityVO> activityDetail(@PathVariable Long activityId) {
        return seckillActivityService.detail(activityId);
    }

    @NoNeedLogin
    @Operation(summary = "收货地址列表")
    @GetMapping("/mall/h5/address/list")
    public ResponseDTO<List<MallAddressVO>> addressList() {
        return mallMemberService.listAddress();
    }

    @NoNeedLogin
    @Operation(summary = "保存收货地址")
    @PostMapping("/mall/h5/address/save")
    public ResponseDTO<String> saveAddress(@RequestBody @Valid MallAddressForm form) {
        return mallMemberService.saveAddress(form);
    }

    @NoNeedLogin
    @Operation(summary = "创建秒杀订单", description = "一单一种商品，qty 自选且不超过每人限购与库存；下单后状态=待付款(10)")
    @PostMapping("/mall/h5/order/create")
    @RepeatSubmit
    public ResponseDTO<MallOrderVO> createOrder(@RequestBody @Valid MallCreateOrderForm form) {
        return mallOrderService.create(form);
    }

    @NoNeedLogin
    @Operation(summary = "提交付款凭证", description = "待付款订单上传截图与说明后变为待商家确认(15)，不再直接标记已支付")
    @PostMapping("/mall/h5/order/pay-proof")
    @RepeatSubmit
    public ResponseDTO<MallOrderVO> payProof(@RequestBody @Valid MallPayProofForm form) {
        return mallOrderService.submitPayProof(form);
    }

    @NoNeedLogin
    @Operation(summary = "我的订单列表")
    @GetMapping("/mall/h5/order/list")
    public ResponseDTO<List<MallOrderVO>> orderList(@RequestParam(required = false) Integer orderStatus) {
        return mallOrderService.listMine(orderStatus);
    }

    @NoNeedLogin
    @Operation(summary = "订单详情", description = "待付款需同时调 /mall/h5/config 取商家收款码")
    @GetMapping("/mall/h5/order/{orderId}")
    public ResponseDTO<MallOrderVO> orderDetail(@PathVariable Long orderId) {
        return mallOrderService.detail(orderId, false);
    }

    @NoNeedLogin
    @Operation(summary = "秒杀单拉起微信支付")
    @PostMapping("/mall/h5/order/wechat/prepay")
    @RepeatSubmit
    public ResponseDTO<MallWechatPayVO> wechatPrepay(@RequestBody @Valid MallWechatPrepayForm form, HttpServletRequest request) {
        if (StringUtils.isBlank(form.getClientIp())) {
            form.setClientIp(JakartaServletUtil.getClientIP(request));
        }
        return mallWechatPayService.prepay(form);
    }

    @NoNeedLogin
    @Operation(summary = "演示模式模拟微信支付成功")
    @PostMapping("/mall/h5/order/wechat/mock-pay/{orderId}")
    @RepeatSubmit
    public ResponseDTO<String> wechatMockPay(@PathVariable Long orderId) {
        return mallWechatPayService.mockPay(orderId);
    }

    @NoNeedLogin
    @Operation(summary = "微信网页授权地址")
    @GetMapping("/mall/h5/wechat/oauth-url")
    public ResponseDTO<String> wechatOauthUrl(@RequestParam String redirectUri, @RequestParam(required = false) String state) {
        return mallWechatPayService.oauthUrl(redirectUri, state);
    }

    @NoNeedLogin
    @Operation(summary = "微信网页授权换 openid")
    @GetMapping("/mall/h5/wechat/oauth")
    public ResponseDTO<String> wechatOauth(@RequestParam String code) {
        return mallWechatPayService.oauthCallback(code);
    }

    @NoNeedLogin
    @Operation(summary = "秒杀单拉起支付宝支付")
    @PostMapping("/mall/h5/order/alipay/prepay")
    @RepeatSubmit
    public ResponseDTO<MallAlipayPayVO> alipayPrepay(@RequestBody @Valid MallWechatPrepayForm form) {
        return mallAlipayPayService.prepay(form);
    }

    @NoNeedLogin
    @Operation(summary = "演示模式模拟支付宝支付成功")
    @PostMapping("/mall/h5/order/alipay/mock-pay/{orderId}")
    @RepeatSubmit
    public ResponseDTO<String> alipayMockPay(@PathVariable Long orderId) {
        return mallAlipayPayService.mockPay(orderId);
    }

    @NoNeedLogin
    @Operation(summary = "确认收货")
    @PostMapping("/mall/h5/order/receive/{orderId}")
    public ResponseDTO<String> receive(@PathVariable Long orderId) {
        return mallOrderService.confirmReceive(orderId);
    }

    @NoNeedLogin
    @Operation(summary = "我的分销佣金")
    @PostMapping("/mall/h5/commission/query")
    public ResponseDTO<PageResult<MallCommissionVO>> myCommission(@RequestBody @Valid MallCommissionQueryForm queryForm) {
        return mallOrderService.queryCommission(queryForm, true);
    }

    @NoNeedLogin
    @Operation(summary = "我的团队")
    @GetMapping("/mall/h5/team")
    public ResponseDTO<List<MallMemberVO>> team() {
        ResponseDTO<List<MallMemberEntity>> res = mallOrderService.myTeam();
        if (!Boolean.TRUE.equals(res.getOk())) {
            return ResponseDTO.error(res);
        }
        return ResponseDTO.ok(SmartBeanUtil.copyList(res.getData(), MallMemberVO.class));
    }
}
