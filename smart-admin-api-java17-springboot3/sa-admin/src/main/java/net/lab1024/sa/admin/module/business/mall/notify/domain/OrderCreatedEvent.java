package net.lab1024.sa.admin.module.business.mall.notify.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * OrderCreated MQ payload.
 */
@Data
public class OrderCreatedEvent {

    private String eventId;

    private String eventType;

    private String occurredAt;

    /** orderNo */
    private String orderId;

    /** memberId as string */
    private String userId;

    private String mobile;

    private BigDecimal amount;

    private String currency;
}
