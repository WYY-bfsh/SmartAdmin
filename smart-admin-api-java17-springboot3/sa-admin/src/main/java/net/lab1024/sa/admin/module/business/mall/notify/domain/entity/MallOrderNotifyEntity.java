package net.lab1024.sa.admin.module.business.mall.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Mall order notify record (SMS channel).
 */
@Data
@TableName("t_mall_order_notify")
public class MallOrderNotifyEntity {

    public static final String CHANNEL_SMS = "SMS";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long memberId;

    private String channel;

    private String status;

    private String eventId;

    private String mobile;

    private String content;

    private String providerMsgId;

    private String errorMsg;

    private Integer retryCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
