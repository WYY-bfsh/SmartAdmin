package net.lab1024.sa.admin.module.business.customer.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.customer.domain.entity.TicketEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.TicketQueryForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.TicketVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客服工单
 */
@Mapper
public interface TicketDao extends BaseMapper<TicketEntity> {

    List<TicketVO> query(Page<?> page, @Param("query") TicketQueryForm query);

    TicketEntity selectByTicketNo(@Param("ticketNo") String ticketNo);
}