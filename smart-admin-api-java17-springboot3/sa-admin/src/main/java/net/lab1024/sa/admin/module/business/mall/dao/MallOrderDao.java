package net.lab1024.sa.admin.module.business.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MallOrderDao extends BaseMapper<MallOrderEntity> {

    int countValidByMemberActivity(@Param("memberId") Long memberId, @Param("activityId") Long activityId);

    int closeIfWaitPayExpired(@Param("orderId") Long orderId);

    int submitProofIfWaitPay(@Param("orderId") Long orderId,
                             @Param("payProofUrl") String payProofUrl,
                             @Param("payNote") String payNote);

    int confirmPayIfWaitConfirm(@Param("orderId") Long orderId);

    int rejectPayIfWaitConfirm(@Param("orderId") Long orderId, @Param("remark") String remark);
}
