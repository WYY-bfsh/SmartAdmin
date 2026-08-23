package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_media_music_song")
public class MusicSongEntity {

    @TableId(type = IdType.AUTO)
    private Long songId;

    private String name;

    private String artist;

    private String album;

    private String duration;

    private String audioUrl;

    private String coverUrl;

    private String lyric;

    private Boolean deletedFlag;
}
