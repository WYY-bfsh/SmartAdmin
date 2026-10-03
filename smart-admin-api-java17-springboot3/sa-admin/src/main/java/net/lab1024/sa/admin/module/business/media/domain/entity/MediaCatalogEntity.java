package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_media_catalog")
public class MediaCatalogEntity {

    @TableId(type = IdType.AUTO)
    private Long catalogId;

    private String kind;

    private String payload;

    private Boolean deletedFlag;

    private LocalDateTime createTime;
}
