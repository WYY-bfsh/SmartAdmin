package net.lab1024.sa.admin.module.business.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeAddForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeUpdateForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.KnowledgeVO;
import net.lab1024.sa.admin.module.business.customer.service.KnowledgeService;
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
 * 知识库
 */
@OperateLog
@RestController
@Tag(name = AdminSwaggerTagConst.Business.CUSTOMER_KNOWLEDGE)
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    @Operation(summary = "分页查询知识库")
    @PostMapping("/customer/knowledge/query")
    @SaCheckPermission("cs:knowledge:query")
    public ResponseDTO<PageResult<KnowledgeVO>> query(@RequestBody @Valid KnowledgeQueryForm queryForm) {
        return knowledgeService.query(queryForm);
    }

    @Operation(summary = "知识库详情")
    @GetMapping("/customer/knowledge/detail/{knowledgeId}")
    @SaCheckPermission("cs:knowledge:query")
    public ResponseDTO<KnowledgeVO> detail(@PathVariable Long knowledgeId) {
        return knowledgeService.detail(knowledgeId);
    }

    @Operation(summary = "新增知识")
    @PostMapping("/customer/knowledge/add")
    @SaCheckPermission("cs:knowledge:edit")
    @RepeatSubmit
    public ResponseDTO<KnowledgeVO> add(@RequestBody @Valid KnowledgeAddForm addForm) {
        return knowledgeService.add(addForm);
    }

    @Operation(summary = "更新知识")
    @PostMapping("/customer/knowledge/update")
    @SaCheckPermission("cs:knowledge:edit")
    @RepeatSubmit
    public ResponseDTO<KnowledgeVO> update(@RequestBody @Valid KnowledgeUpdateForm updateForm) {
        return knowledgeService.update(updateForm);
    }

    @Operation(summary = "删除知识")
    @GetMapping("/customer/knowledge/delete/{knowledgeId}")
    @SaCheckPermission("cs:knowledge:edit")
    public ResponseDTO<String> delete(@PathVariable Long knowledgeId) {
        return knowledgeService.delete(knowledgeId);
    }
}