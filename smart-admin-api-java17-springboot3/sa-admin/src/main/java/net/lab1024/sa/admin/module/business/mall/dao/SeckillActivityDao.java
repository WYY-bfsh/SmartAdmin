package net.lab1024.sa.admin.module.business.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.mall.domain.entity.SeckillActivityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeckillActivityDao extends BaseMapper<SeckillActivityEntity> {

    int deductStock(@Param("activityId") Long activityId, @Param("qty") Integer qty);

    int restoreStock(@Param("activityId") Long activityId, @Param("qty") Integer qty);
}
