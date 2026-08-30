package net.lab1024.sa.admin.module.business.mall.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MallConfigVO {

    private Integer concurrentLimit;
    private Integer payTimeoutMinutes;
    private BigDecimal defaultCommissionRate;
    private BigDecimal defaultCommissionRateL2;
    private Boolean kuaidi100Enabled;
    private String h5Path;
}
