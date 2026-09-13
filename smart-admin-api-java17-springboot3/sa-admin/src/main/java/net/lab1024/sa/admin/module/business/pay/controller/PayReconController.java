package net.lab1024.sa.admin.module.business.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconBatchQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconHandleForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconItemQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconPullForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconBatchVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconItemVO;
import net.lab1024.sa.admin.module.business.pay.service.PayReconService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.operatelog.annotation.OperateLog;
import net.lab1024.sa.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 支付对账
 */
@OperateLog
@RestController
@Tag(name = AdminSwaggerTagConst.Business.PAY_RECON)
public class PayReconController {

    @Resource
    private PayReconService payReconService;

    @Operation(summary = "对账批次分页")
    @PostMapping("/pay/recon/batch/query")
    @SaCheckPermission("pay:recon:query")
    public ResponseDTO<PageResult<PayReconBatchVO>> queryBatch(@RequestBody @Valid PayReconBatchQueryForm queryForm) {
        return payReconService.queryBatch(queryForm);
    }

    @Operation(summary = "对账批次详情")
    @GetMapping("/pay/recon/batch/{batchId}")
    @SaCheckPermission("pay:recon:query")
    public ResponseDTO<PayReconBatchVO> detail(@PathVariable Long batchId) {
        return payReconService.detail(batchId);
    }

    @Operation(summary = "对账明细分页")
    @PostMapping("/pay/recon/item/query")
    @SaCheckPermission("pay:recon:query")
    public ResponseDTO<PageResult<PayReconItemVO>> queryItem(@RequestBody @Valid PayReconItemQueryForm queryForm) {
        return payReconService.queryItem(queryForm);
    }

    @Operation(summary = "拉取渠道账单并对账")
    @PostMapping("/pay/recon/pull")
    @SaCheckPermission("pay:recon:pull")
    @RepeatSubmit
    public ResponseDTO<PayReconBatchVO> pull(@RequestBody @Valid PayReconPullForm form) {
        return payReconService.pull(form);
    }

    @Operation(summary = "演示对账")
    @PostMapping("/pay/recon/mock")
    @SaCheckPermission("pay:recon:mock")
    @RepeatSubmit
    public ResponseDTO<PayReconBatchVO> mock(@RequestBody @Valid PayReconPullForm form) {
        return payReconService.mock(form);
    }

    @Operation(summary = "上传账单并对账")
    @PostMapping("/pay/recon/upload")
    @SaCheckPermission("pay:recon:upload")
    @RepeatSubmit
    public ResponseDTO<PayReconBatchVO> upload(@RequestParam Integer payChannel,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billDate,
                                               @RequestParam MultipartFile file) {
        return payReconService.upload(payChannel, billDate, file);
    }

    @Operation(summary = "人工核销差异")
    @PostMapping("/pay/recon/handle")
    @SaCheckPermission("pay:recon:handle")
    @RepeatSubmit
    public ResponseDTO<String> handle(@RequestBody @Valid PayReconHandleForm form) {
        return payReconService.handle(form);
    }

    @Operation(summary = "删除对账批次")
    @GetMapping("/pay/recon/batch/delete/{batchId}")
    @SaCheckPermission("pay:recon:delete")
    @RepeatSubmit
    public ResponseDTO<String> deleteBatch(@PathVariable Long batchId) {
        return payReconService.deleteBatch(batchId);
    }

    @Operation(summary = "导出对账明细")
    @GetMapping("/pay/recon/item/export/{batchId}")
    @SaCheckPermission("pay:recon:export")
    public void exportItem(@PathVariable Long batchId, HttpServletResponse response) throws IOException {
        payReconService.exportItem(batchId, response);
    }
}
