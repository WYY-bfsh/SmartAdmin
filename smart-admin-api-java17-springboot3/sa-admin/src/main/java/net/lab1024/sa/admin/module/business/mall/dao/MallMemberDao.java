package net.lab1024.sa.admin.module.business.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MallMemberDao extends BaseMapper<MallMemberEntity> {

    MallMemberEntity selectByPhone(@Param("phone") String phone);

    MallMemberEntity selectByInviteCode(@Param("inviteCode") String inviteCode);
}
