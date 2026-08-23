package net.lab1024.sa.admin.module.business.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.customer.constant.TicketMessageTypeEnum;
import net.lab1024.sa.admin.module.business.customer.constant.TicketStatusEnum;
import net.lab1024.sa.admin.module.business.customer.dao.TicketDao;
import net.lab1024.sa.admin.module.business.customer.dao.TicketMessageDao;
import net.lab1024.sa.admin.module.business.customer.domain.entity.TicketEntity;
import net.lab1024.sa.admin.module.business.customer.domain.entity.TicketMessageEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketCreateForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketReplyForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.TicketMessageVO;
import net.lab1024.sa.admin.module.business.customer.domain.vo.TicketVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客服工单
 */
@Slf4j
@Service
public class TicketService {

    private static final DateTimeFormatter TICKET_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private TicketDao ticketDao;

    @Resource
    private TicketMessageDao ticketMessageDao;

    public ResponseDTO<PageResult<TicketVO>> query(TicketQueryForm queryForm) {
        queryForm.setDeletedFlag(false);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<TicketVO> list = ticketDao.query(page, queryForm);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<TicketVO> detail(Long ticketId) {
        TicketEntity entity = ticketDao.selectById(ticketId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("工单不存在");
        }
        TicketVO vo = SmartBeanUtil.copy(entity, TicketVO.class);
        // 加载消息列表
        List<TicketMessageEntity> messages = ticketMessageDao.selectByTicketId(ticketId);
        vo.setMessageList(SmartBeanUtil.copyList(messages, TicketMessageVO.class));
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<TicketVO> create(TicketCreateForm createForm) {
        String ticketNo = generateTicketNo();
        TicketEntity entity = SmartBeanUtil.copy(createForm, TicketEntity.class);
        entity.setTicketNo(ticketNo);
        entity.setStatus(TicketStatusEnum.WAIT_HANDLE.getValue());
        entity.setReplyCount(0);
        entity.setCreateUserId(AdminRequestUtil.getRequestUserId());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        ticketDao.insert(entity);

        TicketVO vo = SmartBeanUtil.copy(entity, TicketVO.class);
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<TicketMessageVO> reply(TicketReplyForm replyForm) {
        TicketEntity ticket = requireTicket(replyForm.getTicketId());
        if (TicketStatusEnum.CLOSED.equalsValue(ticket.getStatus())) {
            return ResponseDTO.userErrorParam("工单已关闭，无法回复");
        }

        // 保存回复消息
        TicketMessageEntity message = new TicketMessageEntity();
        message.setTicketId(replyForm.getTicketId());
        message.setMessageType(TicketMessageTypeEnum.SERVICE.getValue());
        message.setContent(replyForm.getContent());
        message.setCreateUserId(AdminRequestUtil.getRequestUserId());
        message.setCreateName("客服");
        message.setDeletedFlag(Boolean.FALSE);
        message.setCreateTime(LocalDateTime.now());
        ticketMessageDao.insert(message);

        // 更新工单状态
        if (TicketStatusEnum.WAIT_HANDLE.equalsValue(ticket.getStatus())) {
            ticket.setStatus(TicketStatusEnum.HANDLING.getValue());
            ticket.setHandlerUserId(AdminRequestUtil.getRequestUserId());
            ticket.setHandleTime(LocalDateTime.now());
        } else {
            ticket.setStatus(TicketStatusEnum.REPLIED.getValue());
        }
        ticket.setReplyCount(ticket.getReplyCount() == null ? 1 : ticket.getReplyCount() + 1);
        ticketDao.updateById(ticket);

        return ResponseDTO.ok(SmartBeanUtil.copy(message, TicketMessageVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> close(Long ticketId) {
        TicketEntity ticket = requireTicket(ticketId);
        if (TicketStatusEnum.CLOSED.equalsValue(ticket.getStatus())) {
            return ResponseDTO.userErrorParam("工单已关闭");
        }
        ticket.setStatus(TicketStatusEnum.CLOSED.getValue());
        ticket.setCloseTime(LocalDateTime.now());

        // 添加系统关闭消息
        TicketMessageEntity message = new TicketMessageEntity();
        message.setTicketId(ticketId);
        message.setMessageType(TicketMessageTypeEnum.SYSTEM.getValue());
        message.setContent("工单已关闭");
        message.setCreateName("系统");
        message.setDeletedFlag(Boolean.FALSE);
        message.setCreateTime(LocalDateTime.now());
        ticketMessageDao.insert(message);

        ticketDao.updateById(ticket);
        return ResponseDTO.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long ticketId) {
        TicketEntity ticket = requireTicket(ticketId);
        ticket.setDeletedFlag(Boolean.TRUE);
        ticketDao.updateById(ticket);
        return ResponseDTO.ok();
    }

    private TicketEntity requireTicket(Long ticketId) {
        TicketEntity entity = ticketDao.selectById(ticketId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            throw new BusinessException("工单不存在");
        }
        return entity;
    }

    private String generateTicketNo() {
        return "CS" + LocalDateTime.now().format(TICKET_NO_TIME) + RandomStringUtils.randomNumeric(4);
    }
}