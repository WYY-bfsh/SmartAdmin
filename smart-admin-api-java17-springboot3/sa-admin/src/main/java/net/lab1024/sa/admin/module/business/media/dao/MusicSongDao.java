package net.lab1024.sa.admin.module.business.media.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.media.domain.entity.MusicSongEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MusicSongDao extends BaseMapper<MusicSongEntity> {
}
