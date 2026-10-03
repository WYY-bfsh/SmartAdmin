package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_media_user_action")
public class MediaUserActionEntity {

    @TableId(type = IdType.AUTO)
    private Long actionId;

    private Long userId;

    private String actionType;

    private Long bizId;

    private String extra;

    private LocalDateTime updateTime;
}
