package net.lab1024.sa.admin.module.business.pay.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayOrderEntity;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 微信支付订单
 */
@Mapper
public interface PayOrderDao extends BaseMapper<PayOrderEntity> {

    List<PayOrderVO> query(Page<?> page, @Param("query") PayOrderQueryForm query);

    PayOrderEntity selectByOrderNo(@Param("orderNo") String orderNo);
}
