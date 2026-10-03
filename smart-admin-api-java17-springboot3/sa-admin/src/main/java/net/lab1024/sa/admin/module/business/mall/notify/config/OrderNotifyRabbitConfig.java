package net.lab1024.sa.admin.module.business.mall.notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Order notify RabbitMQ: topic exchange + durable queue + DLX/DLQ + JSON converter.
 */
@Configuration
public class OrderNotifyRabbitConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order.event";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_NOTIFY_QUEUE = "order.notify.queue";

    public static final String ORDER_EVENT_DLX = "order.event.dlx";
    public static final String ORDER_NOTIFY_DLQ = "order.notify.dlq";
    public static final String ORDER_NOTIFY_DLQ_ROUTING_KEY = "order.notify.dlq";

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange orderEventDlx() {
        return new TopicExchange(ORDER_EVENT_DLX, true, false);
    }

    @Bean
    public Queue orderNotifyQueue() {
        return QueueBuilder.durable(ORDER_NOTIFY_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EVENT_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_NOTIFY_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderNotifyDlq() {
        return QueueBuilder.durable(ORDER_NOTIFY_DLQ).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderNotifyQueue, TopicExchange orderEventExchange) {
        return BindingBuilder.bind(orderNotifyQueue).to(orderEventExchange).with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding orderNotifyDlqBinding(Queue orderNotifyDlq, TopicExchange orderEventDlx) {
        return BindingBuilder.bind(orderNotifyDlq).to(orderEventDlx).with(ORDER_NOTIFY_DLQ_ROUTING_KEY);
    }

    /**
     * JSON body for OrderCreatedEvent. Skip if project already defines a MessageConverter bean.
     */
    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter orderNotifyJacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
