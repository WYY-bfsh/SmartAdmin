package net.lab1024.sa.admin.module.business.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MallOrderDao extends BaseMapper<MallOrderEntity> {

    int countValidByMemberActivity(@Param("memberId") Long memberId, @Param("activityId") Long activityId);
}
