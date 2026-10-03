package net.lab1024.sa.admin.module.business.mall.notify.listener;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.notify.config.OrderNotifyRabbitConfig;
import net.lab1024.sa.admin.module.business.mall.notify.domain.OrderCreatedEvent;
import net.lab1024.sa.admin.module.business.mall.notify.domain.entity.MallOrderNotifyEntity;
import net.lab1024.sa.admin.module.business.mall.notify.service.MallOrderNotifyService;
import net.lab1024.sa.admin.module.business.mall.notify.sms.TencentSmsClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Consume OrderCreated; send Tencent SMS; idempotent by orderNo; DLX after max retries.
 */
@Slf4j
@Component
public class OrderNotifyListener {

    private static final int MAX_RETRY = 3;

    @Resource
    private MallOrderNotifyService mallOrderNotifyService;

    @Resource
    private TencentSmsClient tencentSmsClient;

    @RabbitListener(queues = OrderNotifyRabbitConfig.ORDER_NOTIFY_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (event == null || !StringUtils.hasText(event.getOrderId())) {
                log.warn("[order.notify] non-recoverable: blank orderId, nack requeue=false -> DLX");
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            String orderNo = event.getOrderId();
            if (mallOrderNotifyService.isSuccess(orderNo)) {
                log.info("[order.notify] idempotent skip SUCCESS orderNo={}", orderNo);
                channel.basicAck(deliveryTag, false);
                return;
            }

            String mobile = event.getMobile();
            if (!StringUtils.hasText(mobile)) {
                log.warn("[order.notify] non-recoverable: blank mobile orderNo={}, nack requeue=false", orderNo);
                Long memberId = parseMemberId(event.getUserId());
                MallOrderNotifyEntity row = mallOrderNotifyService.upsertPending(
                        orderNo, memberId, event.getEventId(), mobile, "blank mobile");
                mallOrderNotifyService.markFailedAndIncRetry(row.getId(), "blank mobile");
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            Long memberId = parseMemberId(event.getUserId());
            String amountText = formatAmount(event.getAmount());
            String content = "orderNo=" + orderNo + ", amount=" + amountText;
            MallOrderNotifyEntity row = mallOrderNotifyService.upsertPending(
                    orderNo, memberId, event.getEventId(), mobile, content);

            int deathCount = readDeathCount(message);
            int dbRetry = row.getRetryCount() == null ? 0 : row.getRetryCount();
            if (deathCount >= MAX_RETRY || dbRetry >= MAX_RETRY) {
                log.warn("[order.notify] max retries reached orderNo={} death={} dbRetry={}, nack requeue=false",
                        orderNo, deathCount, dbRetry);
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            TencentSmsClient.SmsSendResult result = tencentSmsClient.sendOrderCreated(mobile, orderNo, amountText);
            if (result.isSuccess()) {
                String providerId = StringUtils.hasText(result.getSerialNo())
                        ? result.getSerialNo()
                        : result.getRequestId();
                mallOrderNotifyService.markSuccess(row.getId(), providerId, content);
                channel.basicAck(deliveryTag, false);
                log.info("[order.notify] SMS SUCCESS orderNo={} requestId={}", orderNo, result.getRequestId());
                return;
            }

            MallOrderNotifyEntity failed = mallOrderNotifyService.markFailedAndIncRetry(
                    row.getId(), result.getMessage());
            int nextRetry = failed == null || failed.getRetryCount() == null ? dbRetry + 1 : failed.getRetryCount();
            if (nextRetry >= MAX_RETRY) {
                log.warn("[order.notify] SMS failed and retry_count>={} orderNo={}, nack requeue=false",
                        MAX_RETRY, orderNo);
                channel.basicNack(deliveryTag, false, false);
            } else {
                log.warn("[order.notify] SMS failed orderNo={} msg={}, nack requeue=true",
                        orderNo, result.getMessage());
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IllegalStateException | IllegalArgumentException nonRetryable) {
            log.error("[order.notify] non-retryable: {}", nonRetryable.getMessage());
            try {
                if (event != null && StringUtils.hasText(event.getOrderId())) {
                    Long memberId = parseMemberId(event.getUserId());
                    MallOrderNotifyEntity row = mallOrderNotifyService.upsertPending(
                            event.getOrderId(), memberId, event.getEventId(), event.getMobile(),
                            nonRetryable.getMessage());
                    mallOrderNotifyService.markFailedAndIncRetry(row.getId(), nonRetryable.getMessage());
                }
            } catch (Exception ignore) {
                // ignore secondary persist errors
            }
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("[order.notify] unexpected error, nack requeue=true: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private static Long parseMemberId(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            return Long.valueOf(userId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @SuppressWarnings("unchecked")
    private static int readDeathCount(Message message) {
        try {
            List<Map<String, Object>> deaths =
                    (List<Map<String, Object>>) message.getMessageProperties().getHeaders().get("x-death");
            if (deaths == null || deaths.isEmpty()) {
                return 0;
            }
            Object count = deaths.get(0).get("count");
            if (count instanceof Number) {
                return ((Number) count).intValue();
            }
        } catch (Exception ignore) {
            // ignore
        }
        return 0;
    }
}
