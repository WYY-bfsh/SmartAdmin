package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_seckill_activity")
public class SeckillActivityEntity {

    @TableId(type = IdType.AUTO)
    private Long activityId;

    private String title;

    private String goodsName;

    private String coverUrl;

    private String detail;

    private BigDecimal originPrice;

    private BigDecimal seckillPrice;

    private Integer stock;

    private Integer soldCount;

    private Integer perLimit;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 本场同时抢购人数上限，空则用全局配置
     */
    private Integer concurrentLimit;

    private BigDecimal commissionRate;

    /**
     * 二级分销比例，空则用全局默认
     */
    private BigDecimal commissionRateL2;

    private Boolean enabledFlag;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
