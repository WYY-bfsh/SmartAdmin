package net.lab1024.sa.admin.module.business.customer.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.customer.domain.entity.TicketMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单消息
 */
@Mapper
public interface TicketMessageDao extends BaseMapper<TicketMessageEntity> {

    List<TicketMessageEntity> selectByTicketId(@Param("ticketId") Long ticketId);
}