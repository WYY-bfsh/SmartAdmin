package net.lab1024.sa.admin.module.business.customer.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.customer.domain.entity.KnowledgeEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.KnowledgeQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.KnowledgeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库
 */
@Mapper
public interface KnowledgeDao extends BaseMapper<KnowledgeEntity> {

    List<KnowledgeVO> query(Page<?> page, @Param("query") KnowledgeQueryForm query);
}