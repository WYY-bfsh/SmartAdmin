package net.lab1024.sa.admin.module.business.mall.job;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.service.MallOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时未支付关单并回库存
 */
@Slf4j
@Component
public class MallOrderTimeoutJob {

    @Resource
    private MallOrderService mallOrderService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeExpired() {
        try {
            int count = mallOrderService.closeExpired();
            if (count > 0) {
                log.info("秒杀超时关单 {} 笔", count);
            }
        } catch (Exception e) {
            log.warn("秒杀超时关单失败: {}", e.getMessage());
        }
    }
}
