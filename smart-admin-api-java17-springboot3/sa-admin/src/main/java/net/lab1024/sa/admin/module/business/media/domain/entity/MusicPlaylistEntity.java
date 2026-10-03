package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_media_music_playlist")
public class MusicPlaylistEntity {

    @TableId(type = IdType.AUTO)
    private Long playlistId;

    private String name;

    private String playCount;

    private Integer songCount;

    private String coverUrl;

    private String description;

    private String songIds;
}
