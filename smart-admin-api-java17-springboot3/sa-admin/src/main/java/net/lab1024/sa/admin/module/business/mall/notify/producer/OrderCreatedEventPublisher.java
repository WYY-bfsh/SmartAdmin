package net.lab1024.sa.admin.module.business.mall.notify.producer;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.notify.config.OrderNotifyRabbitConfig;
import net.lab1024.sa.admin.module.business.mall.notify.domain.OrderCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Publish OrderCreated to RabbitMQ after DB transaction commit.
 * Call from MallOrderService.create via TransactionSynchronization.afterCommit.
 * Do NOT send SMS here.
 */
@Slf4j
@Component
public class OrderCreatedEventPublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * Preferred API when calling from MallOrderService afterCommit.
     * Pass fields from MallOrderEntity: orderNo, memberId, receiverPhone (or member phone), amount.
     */
    public void publishAfterCommit(String orderNo, Long memberId, String mobile, BigDecimal amount) {
        if (orderNo == null || orderNo.isBlank()) {
            log.warn("[order.notify] skip publish: blank orderNo");
            return;
        }
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType("OrderCreated");
        event.setOccurredAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        event.setOrderId(orderNo);
        event.setUserId(memberId == null ? null : String.valueOf(memberId));
        event.setMobile(mobile);
        event.setAmount(amount);
        event.setCurrency("CNY");

        rabbitTemplate.convertAndSend(
                OrderNotifyRabbitConfig.ORDER_EVENT_EXCHANGE,
                OrderNotifyRabbitConfig.ORDER_CREATED_ROUTING_KEY,
                event
        );
        log.info("[order.notify] published OrderCreated eventId={} orderNo={}", event.getEventId(), orderNo);
    }

    /**
     * Accept MallOrderEntity (or any bean with getOrderNo/getMemberId/getAmount/getReceiverPhone).
     * Prefer explicit publishAfterCommit(orderNo, memberId, mobile, amount) when wiring create().
     */
    public void publishAfterCommit(Object mallOrderEntity) {
        if (mallOrderEntity == null) {
            return;
        }
        try {
            String orderNo = invokeString(mallOrderEntity, "getOrderNo");
            Long memberId = invokeLong(mallOrderEntity, "getMemberId");
            BigDecimal amount = invokeBigDecimal(mallOrderEntity, "getAmount");
            String mobile = invokeString(mallOrderEntity, "getReceiverPhone");
            if (mobile == null || mobile.isBlank()) {
                mobile = invokeString(mallOrderEntity, "getMobile");
            }
            publishAfterCommit(orderNo, memberId, mobile, amount);
        } catch (Exception e) {
            log.error("[order.notify] publishAfterCommit failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to publish OrderCreated: " + e.getMessage(), e);
        }
    }

    private static String invokeString(Object target, String method) throws Exception {
        try {
            Object v = target.getClass().getMethod(method).invoke(target);
            return v == null ? null : String.valueOf(v);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static Long invokeLong(Object target, String method) throws Exception {
        try {
            Object v = target.getClass().getMethod(method).invoke(target);
            if (v == null) {
                return null;
            }
            if (v instanceof Long) {
                return (Long) v;
            }
            return Long.valueOf(String.valueOf(v));
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static BigDecimal invokeBigDecimal(Object target, String method) throws Exception {
        try {
            Object v = target.getClass().getMethod(method).invoke(target);
            if (v == null) {
                return null;
            }
            if (v instanceof BigDecimal) {
                return (BigDecimal) v;
            }
            return new BigDecimal(String.valueOf(v));
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }
}
