package net.lab1024.sa.admin.module.business.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.customer.constant.QAStatusEnum;
import net.lab1024.sa.admin.module.business.customer.dao.QADao;
import net.lab1024.sa.admin.module.business.customer.domain.entity.QAEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAAddForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAAnswerForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.QAVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问答
 */
@Slf4j
@Service
public class QAService {

    @Resource
    private QADao qaDao;

    public ResponseDTO<PageResult<QAVO>> query(QAQueryForm queryForm) {
        queryForm.setDeletedFlag(false);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<QAVO> list = qaDao.query(page, queryForm);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<QAVO> detail(Long qaId) {
        QAEntity entity = qaDao.selectById(qaId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("问答不存在");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, QAVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<QAVO> ask(QAAddForm addForm) {
        QAEntity entity = SmartBeanUtil.copy(addForm, QAEntity.class);
        entity.setStatus(QAStatusEnum.WAIT_ANSWER.getValue());
        entity.setCreateUserId(AdminRequestUtil.getRequestUserId());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        qaDao.insert(entity);
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, QAVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<QAVO> answer(QAAnswerForm answerForm) {
        QAEntity entity = requireQA(answerForm.getQaId());
        if (!QAStatusEnum.WAIT_ANSWER.equalsValue(entity.getStatus())) {
            return ResponseDTO.userErrorParam("该问题已处理，无需重复操作");
        }

        if ("reject".equals(answerForm.getAction())) {
            entity.setStatus(QAStatusEnum.REJECTED.getValue());
            entity.setAnswer("【已驳回】" + (answerForm.getAnswer() != null ? answerForm.getAnswer() : ""));
        } else {
            entity.setAnswer(answerForm.getAnswer());
            entity.setStatus(QAStatusEnum.ANSWERED.getValue());
        }
        entity.setAnswerUserId(AdminRequestUtil.getRequestUserId());
        entity.setAnswerTime(LocalDateTime.now());
        qaDao.updateById(entity);

        return ResponseDTO.ok(SmartBeanUtil.copy(entity, QAVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long qaId) {
        QAEntity entity = requireQA(qaId);
        entity.setDeletedFlag(Boolean.TRUE);
        qaDao.updateById(entity);
        return ResponseDTO.ok();
    }

    private QAEntity requireQA(Long qaId) {
        QAEntity entity = qaDao.selectById(qaId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            throw new BusinessException("问答不存在");
        }
        return entity;
    }
}