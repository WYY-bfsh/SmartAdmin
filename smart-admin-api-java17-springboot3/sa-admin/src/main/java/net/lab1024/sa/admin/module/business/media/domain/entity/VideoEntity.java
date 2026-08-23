package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_media_video")
public class VideoEntity {

    @TableId(type = IdType.AUTO)
    private Long videoId;

    private String title;

    private Integer category;

    private String area;

    private Integer year;

    private java.math.BigDecimal score;

    private Integer episodeCount;

    private String updateInfo;

    private String playUrl;

    private String coverUrl;

    private String intro;

    private Boolean deletedFlag;

    private String source;

    private String sourceId;
}
