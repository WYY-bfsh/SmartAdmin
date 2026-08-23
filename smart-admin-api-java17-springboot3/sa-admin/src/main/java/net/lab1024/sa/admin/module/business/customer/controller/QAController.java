package net.lab1024.sa.admin.module.business.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAAddForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAAnswerForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.QAVO;
import net.lab1024.sa.admin.module.business.customer.service.QAService;
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
 * 问答
 */
@OperateLog
@RestController
@Tag(name = AdminSwaggerTagConst.Business.CUSTOMER_QA)
public class QAController {

    @Resource
    private QAService qaService;

    @Operation(summary = "分页查询问答")
    @PostMapping("/customer/qa/query")
    @SaCheckPermission("cs:qa:query")
    public ResponseDTO<PageResult<QAVO>> query(@RequestBody @Valid QAQueryForm queryForm) {
        return qaService.query(queryForm);
    }

    @Operation(summary = "问答详情")
    @GetMapping("/customer/qa/detail/{qaId}")
    @SaCheckPermission("cs:qa:query")
    public ResponseDTO<QAVO> detail(@PathVariable Long qaId) {
        return qaService.detail(qaId);
    }

    @Operation(summary = "提交提问")
    @PostMapping("/customer/qa/ask")
    @SaCheckPermission("cs:qa:ask")
    @RepeatSubmit
    public ResponseDTO<QAVO> ask(@RequestBody @Valid QAAddForm addForm) {
        return qaService.ask(addForm);
    }

    @Operation(summary = "回答/驳回")
    @PostMapping("/customer/qa/answer")
    @SaCheckPermission("cs:qa:answer")
    @RepeatSubmit
    public ResponseDTO<QAVO> answer(@RequestBody @Valid QAAnswerForm answerForm) {
        return qaService.answer(answerForm);
    }

    @Operation(summary = "删除问答")
    @GetMapping("/customer/qa/delete/{qaId}")
    @SaCheckPermission("cs:qa:delete")
    public ResponseDTO<String> delete(@PathVariable Long qaId) {
        return qaService.delete(qaId);
    }
}