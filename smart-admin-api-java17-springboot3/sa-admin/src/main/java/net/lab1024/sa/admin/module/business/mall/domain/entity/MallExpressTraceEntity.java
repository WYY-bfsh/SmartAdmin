package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_mall_express_trace")
public class MallExpressTraceEntity {

    @TableId(type = IdType.AUTO)
    private Long traceId;

    private Long orderId;

    private String ftime;

    private String context;

    private String statusText;

    private Integer sortNo;

    private LocalDateTime createTime;
}
