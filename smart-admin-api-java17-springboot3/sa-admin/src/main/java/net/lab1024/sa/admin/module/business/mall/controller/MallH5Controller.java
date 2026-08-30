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
import net.lab1024.sa.admin.module.business.mall.domain.form.MallRegisterForm;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = AdminSwaggerTagConst.Business.MALL_H5)
public class MallH5Controller {

    @Resource
    private MallMemberService mallMemberService;

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private MallOrderService mallOrderService;

    @NoNeedLogin
    @Operation(summary = "商城配置")
    @GetMapping("/mall/h5/config")
    public ResponseDTO<MallConfigVO> config() {
        return ResponseDTO.ok(seckillActivityService.config());
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/avatar/upload")
    public ResponseDTO<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return mallMemberService.uploadAvatar(file);
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/register")
    public ResponseDTO<MallMemberVO> register(@RequestBody @Valid MallRegisterForm form) {
        return mallMemberService.register(form);
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/login")
    public ResponseDTO<MallMemberVO> login(@RequestBody @Valid MallLoginForm form) {
        return mallMemberService.login(form);
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/me")
    public ResponseDTO<MallMemberVO> me() {
        return mallMemberService.me();
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/activity/list")
    public ResponseDTO<List<SeckillActivityVO>> activityList() {
        return seckillActivityService.listForH5();
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/activity/{activityId}")
    public ResponseDTO<SeckillActivityVO> activityDetail(@PathVariable Long activityId) {
        return seckillActivityService.detail(activityId);
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/address/list")
    public ResponseDTO<List<MallAddressVO>> addressList() {
        return mallMemberService.listAddress();
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/address/save")
    public ResponseDTO<String> saveAddress(@RequestBody @Valid MallAddressForm form) {
        return mallMemberService.saveAddress(form);
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/order/create")
    @RepeatSubmit
    public ResponseDTO<MallOrderVO> createOrder(@RequestBody @Valid MallCreateOrderForm form) {
        return mallOrderService.create(form);
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/order/pay/{orderId}")
    @RepeatSubmit
    public ResponseDTO<MallOrderVO> pay(@PathVariable Long orderId) {
        return mallOrderService.mockPay(orderId);
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/order/list")
    public ResponseDTO<List<MallOrderVO>> orderList(@RequestParam(required = false) Integer orderStatus) {
        return mallOrderService.listMine(orderStatus);
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/order/{orderId}")
    public ResponseDTO<MallOrderVO> orderDetail(@PathVariable Long orderId) {
        return mallOrderService.detail(orderId, false);
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/order/receive/{orderId}")
    public ResponseDTO<String> receive(@PathVariable Long orderId) {
        return mallOrderService.confirmReceive(orderId);
    }

    @NoNeedLogin
    @PostMapping("/mall/h5/commission/query")
    public ResponseDTO<PageResult<MallCommissionVO>> myCommission(@RequestBody @Valid MallCommissionQueryForm queryForm) {
        return mallOrderService.queryCommission(queryForm, true);
    }

    @NoNeedLogin
    @GetMapping("/mall/h5/team")
    public ResponseDTO<List<MallMemberVO>> team() {
        ResponseDTO<List<MallMemberEntity>> res = mallOrderService.myTeam();
        if (!Boolean.TRUE.equals(res.getOk())) {
            return ResponseDTO.error(res);
        }
        return ResponseDTO.ok(SmartBeanUtil.copyList(res.getData(), MallMemberVO.class));
    }
}
