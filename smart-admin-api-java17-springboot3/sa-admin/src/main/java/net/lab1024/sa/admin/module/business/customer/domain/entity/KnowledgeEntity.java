package net.lab1024.sa.admin.module.business.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服知识库
 */
@Data
@TableName("t_cs_knowledge")
public class KnowledgeEntity {

    @TableId(type = IdType.AUTO)
    private Long knowledgeId;

    /** 标题 */
    private String title;

    /** 分类：1常见问题 2使用教程 3业务说明 4其他 */
    private Integer category;

    /** 内容（富文本） */
    private String content;

    /** 排序 */
    private Integer sort;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建人 */
    private Long createUserId;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}