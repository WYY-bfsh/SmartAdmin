package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_media_video_history")
public class VideoHistoryEntity {

    @TableId(type = IdType.AUTO)
    private Long historyId;

    private Long videoId;

    private Integer episodeNo;

    private Integer progress;

    private Long userId;

    private java.time.LocalDateTime updateTime;
}
