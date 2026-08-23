package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_media_ai_project")
public class AiProjectEntity {

    @TableId(type = IdType.AUTO)
    private Long projectId;

    private String title;

    private Integer scene;

    private Integer status;

    private String duration;

    private String ratio;

    private String coverUrl;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
