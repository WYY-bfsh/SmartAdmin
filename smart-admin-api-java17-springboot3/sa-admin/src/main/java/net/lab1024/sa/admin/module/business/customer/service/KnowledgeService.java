package net.lab1024.sa.admin.module.business.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.customer.dao.KnowledgeDao;
import net.lab1024.sa.admin.module.business.customer.domain.entity.KnowledgeEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeAddForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeUpdateForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.KnowledgeVO;
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
 * 知识库
 */
@Slf4j
@Service
public class KnowledgeService {

    @Resource
    private KnowledgeDao knowledgeDao;

    public ResponseDTO<PageResult<KnowledgeVO>> query(KnowledgeQueryForm queryForm) {
        queryForm.setDeletedFlag(false);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<KnowledgeVO> list = knowledgeDao.query(page, queryForm);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<KnowledgeVO> detail(Long knowledgeId) {
        KnowledgeEntity entity = knowledgeDao.selectById(knowledgeId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("知识不存在");
        }
        // 浏览数+1
        entity.setViewCount(entity.getViewCount() == null ? 1 : entity.getViewCount() + 1);
        knowledgeDao.updateById(entity);

        return ResponseDTO.ok(SmartBeanUtil.copy(entity, KnowledgeVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<KnowledgeVO> add(KnowledgeAddForm addForm) {
        KnowledgeEntity entity = SmartBeanUtil.copy(addForm, KnowledgeEntity.class);
        entity.setViewCount(0);
        entity.setCreateUserId(AdminRequestUtil.getRequestUserId());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        knowledgeDao.insert(entity);
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, KnowledgeVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<KnowledgeVO> update(KnowledgeUpdateForm updateForm) {
        KnowledgeEntity entity = knowledgeDao.selectById(updateForm.getKnowledgeId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("知识不存在");
        }
        SmartBeanUtil.copyProperties(updateForm, entity);
        knowledgeDao.updateById(entity);
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, KnowledgeVO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long knowledgeId) {
        KnowledgeEntity entity = knowledgeDao.selectById(knowledgeId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("知识不存在");
        }
        entity.setDeletedFlag(Boolean.TRUE);
        knowledgeDao.updateById(entity);
        return ResponseDTO.ok();
    }
}