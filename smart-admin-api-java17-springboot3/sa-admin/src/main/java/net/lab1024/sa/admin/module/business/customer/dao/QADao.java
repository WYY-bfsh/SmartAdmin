package net.lab1024.sa.admin.module.business.customer.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.customer.domain.entity.QAEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.QAQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.QAVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 问答
 */
@Mapper
public interface QADao extends BaseMapper<QAEntity> {

    List<QAVO> query(Page<?> page, @Param("query") QAQueryForm query);
}