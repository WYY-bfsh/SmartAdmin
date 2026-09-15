package net.lab1024.sa.admin.module.business.mall.notify.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.mall.notify.domain.entity.MallOrderNotifyEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mall order notify DAO.
 */
@Mapper
public interface MallOrderNotifyDao extends BaseMapper<MallOrderNotifyEntity> {
}
