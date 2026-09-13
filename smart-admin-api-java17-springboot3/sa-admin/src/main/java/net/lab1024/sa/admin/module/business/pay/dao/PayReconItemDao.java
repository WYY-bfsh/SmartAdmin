package net.lab1024.sa.admin.module.business.pay.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayReconItemEntity;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayReconItemQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayReconItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对账明细
 */
@Mapper
public interface PayReconItemDao extends BaseMapper<PayReconItemEntity> {

    List<PayReconItemVO> query(Page<?> page, @Param("query") PayReconItemQueryForm query);

    List<PayReconItemVO> listByBatchId(@Param("batchId") Long batchId);
}
