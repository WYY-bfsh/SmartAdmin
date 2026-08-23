package net.lab1024.sa.admin.module.business.media.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.media.domain.entity.MusicPlaylistEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MusicPlaylistDao extends BaseMapper<MusicPlaylistEntity> {
}
