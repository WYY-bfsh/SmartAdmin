package net.lab1024.sa.admin.module.business.pay.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayReconBatchEntity;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconBatchQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconBatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对账批次
 */
@Mapper
public interface PayReconBatchDao extends BaseMapper<PayReconBatchEntity> {

    List<PayReconBatchVO> query(Page<?> page, @Param("query") PayReconBatchQueryForm query);
}
