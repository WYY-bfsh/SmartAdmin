package net.lab1024.sa.admin.module.business.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_media_ai_material")
public class AiMaterialEntity {

    @TableId(type = IdType.AUTO)
    private Long materialId;

    private String name;

    private Integer type;

    private String duration;

    private String size;

    private String coverUrl;

    private String fileKey;

    private String fileUrl;

    private Boolean deletedFlag;

    private LocalDateTime createTime;
}
