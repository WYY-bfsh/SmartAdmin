package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_media_ai_task")
public class AiTaskEntity {

    @TableId(type = IdType.AUTO)
    private Long taskId;

    private String title;

    private String type;

    private Integer progress;

    private Integer status;

    private String resultUrl;

    private Boolean deletedFlag;

    private LocalDateTime createTime;
}
