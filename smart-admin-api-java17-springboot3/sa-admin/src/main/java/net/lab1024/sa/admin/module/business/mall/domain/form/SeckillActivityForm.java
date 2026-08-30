package net.lab1024.sa.admin.module.business.mall.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillActivityForm {

    private Long activityId;

    @NotBlank(message = "请填写活动标题")
    private String title;

    @NotBlank(message = "请填写商品名称")
    private String goodsName;

    private String coverUrl;

    private String detail;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal originPrice;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal seckillPrice;

    @NotNull
    private Integer stock;

    private Integer perLimit = 1;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @Schema(description = "本场同时抢购人数，空则用全局待定常数")
    private Integer concurrentLimit;

    private BigDecimal commissionRate;

    private Boolean enabledFlag = Boolean.TRUE;
}
