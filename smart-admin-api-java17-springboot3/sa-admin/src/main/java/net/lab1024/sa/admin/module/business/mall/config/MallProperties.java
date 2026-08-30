package net.lab1024.sa.admin.module.business.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 秒杀商城配置。concurrent-limit 为预计同时抢购人数，当前先待定可改。
 */
@Data
@Component
@ConfigurationProperties(prefix = "mall")
public class MallProperties {

    private Seckill seckill = new Seckill();

    private Express express = new Express();

    @Data
    public static class Seckill {
        /**
         * 预计同时抢购人数（限流阈值），待定
         */
        private Integer concurrentLimit = 200;

        private Integer payTimeoutMinutes = 15;

        private BigDecimal defaultCommissionRate = new BigDecimal("0.05");

        /**
         * 二级分销默认比例。千3 = 0.003
         */
        private BigDecimal defaultCommissionRateL2 = new BigDecimal("0.003");
    }

    @Data
    public static class Express {
        private String kuaidi100Key;

        private String kuaidi100Customer;
    }
}
