package net.lab1024.sa.admin.module.business.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketCreateForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketReplyForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.TicketMessageVO;
import net.lab1024.sa.admin.module.business.customer.domain.vo.TicketVO;
import net.lab1024.sa.admin.module.business.customer.service.TicketService;
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
 * 客服工单
 */
@OperateLog
@RestController
@Tag(name = AdminSwaggerTagConst.Business.CUSTOMER_TICKET)
public class TicketController {

    @Resource
    private TicketService ticketService;

    @Operation(summary = "分页查询工单")
    @PostMapping("/customer/ticket/query")
    @SaCheckPermission("cs:ticket:query")
    public ResponseDTO<PageResult<TicketVO>> query(@RequestBody @Valid TicketQueryForm queryForm) {
        return ticketService.query(queryForm);
    }

    @Operation(summary = "工单详情")
    @GetMapping("/customer/ticket/detail/{ticketId}")
    @SaCheckPermission("cs:ticket:query")
    public ResponseDTO<TicketVO> detail(@PathVariable Long ticketId) {
        return ticketService.detail(ticketId);
    }

    @Operation(summary = "创建工单")
    @PostMapping("/customer/ticket/create")
    @SaCheckPermission("cs:ticket:create")
    @RepeatSubmit
    public ResponseDTO<TicketVO> create(@RequestBody @Valid TicketCreateForm createForm) {
        return ticketService.create(createForm);
    }

    @Operation(summary = "回复工单")
    @PostMapping("/customer/ticket/reply")
    @SaCheckPermission("cs:ticket:reply")
    @RepeatSubmit
    public ResponseDTO<TicketMessageVO> reply(@RequestBody @Valid TicketReplyForm replyForm) {
        return ticketService.reply(replyForm);
    }

    @Operation(summary = "关闭工单")
    @GetMapping("/customer/ticket/close/{ticketId}")
    @SaCheckPermission("cs:ticket:close")
    @RepeatSubmit
    public ResponseDTO<String> close(@PathVariable Long ticketId) {
        return ticketService.close(ticketId);
    }

    @Operation(summary = "删除工单")
    @GetMapping("/customer/ticket/delete/{ticketId}")
    @SaCheckPermission("cs:ticket:delete")
    public ResponseDTO<String> delete(@PathVariable Long ticketId) {
        return ticketService.delete(ticketId);
    }
}