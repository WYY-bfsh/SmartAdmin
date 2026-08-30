package net.lab1024.sa.admin.module.business.mall.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallCommissionQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallMemberQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallOrderQueryForm;
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
@Tag(name = AdminSwaggerTagConst.Business.MALL_SECKILL)
public class MallAdminController {

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private MallOrderService mallOrderService;

    @Resource
    private MallMemberService mallMemberService;

    @Resource
    private MallExpressService mallExpressService;

    @Operation(summary = "秒杀配置")
    @GetMapping("/mall/admin/config")
    @SaCheckPermission("mall:activity:query")
    public ResponseDTO<MallConfigVO> config() {
        return ResponseDTO.ok(seckillActivityService.config());
    }

    @PostMapping("/mall/admin/activity/query")
    @SaCheckPermission("mall:activity:query")
    public ResponseDTO<PageResult<SeckillActivityVO>> activityQuery(@RequestBody @Valid SeckillActivityQueryForm queryForm) {
        return seckillActivityService.query(queryForm);
    }

    @PostMapping("/mall/admin/activity/save")
    @SaCheckPermission("mall:activity:save")
    @RepeatSubmit
    public ResponseDTO<String> activitySave(@RequestBody @Valid SeckillActivityForm form) {
        return seckillActivityService.save(form);
    }

    @PostMapping("/mall/admin/order/query")
    @SaCheckPermission("mall:order:query")
    public ResponseDTO<PageResult<MallOrderVO>> orderQuery(@RequestBody @Valid MallOrderQueryForm queryForm) {
        return mallOrderService.queryAdmin(queryForm);
    }

    @GetMapping("/mall/admin/order/{orderId}")
    @SaCheckPermission("mall:order:query")
    public ResponseDTO<MallOrderVO> orderDetail(@PathVariable Long orderId) {
        return mallOrderService.detail(orderId, true);
    }

    @PostMapping("/mall/admin/order/ship")
    @SaCheckPermission("mall:order:ship")
    @RepeatSubmit
    public ResponseDTO<String> ship(@RequestBody @Valid MallShipForm form) {
        return mallOrderService.ship(form);
    }

    @GetMapping("/mall/admin/express/companies")
    @SaCheckPermission("mall:order:query")
    public ResponseDTO<List<ExpressCompanyVO>> companies() {
        return ResponseDTO.ok(mallExpressService.companies());
    }

    @PostMapping("/mall/admin/member/query")
    @SaCheckPermission("mall:member:query")
    public ResponseDTO<PageResult<MallMemberVO>> memberQuery(@RequestBody @Valid MallMemberQueryForm queryForm) {
        return mallMemberService.queryMembers(queryForm);
    }

    @PostMapping("/mall/admin/commission/query")
    @SaCheckPermission("mall:commission:query")
    public ResponseDTO<PageResult<MallCommissionVO>> commissionQuery(@RequestBody @Valid MallCommissionQueryForm queryForm) {
        return mallOrderService.queryCommission(queryForm, false);
    }
}
