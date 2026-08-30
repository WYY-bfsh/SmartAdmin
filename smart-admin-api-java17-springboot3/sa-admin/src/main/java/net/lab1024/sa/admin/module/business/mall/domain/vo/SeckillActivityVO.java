package net.lab1024.sa.admin.module.business.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillActivityVO {

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
    private Integer concurrentLimit;
    private BigDecimal commissionRate;
    private BigDecimal commissionRateL2;
    private Boolean enabledFlag;

    @Schema(description = "10未开始 20进行中 30已结束")
    private Integer saleStatus;

    @Schema(description = "saleStatus 文案")
    private String saleStatusDesc;

    private Long remainSeconds;
    private LocalDateTime createTime;
}
