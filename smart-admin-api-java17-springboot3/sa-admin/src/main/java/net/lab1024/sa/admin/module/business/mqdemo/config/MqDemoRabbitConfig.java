package net.lab1024.sa.admin.module.business.mqdemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 五种基本模式练习：队列 / 交换机 / 绑定
 */
@Configuration
public class MqDemoRabbitConfig {

    public static final String Q_SIMPLE = "mq.demo.simple";
    public static final String Q_WORK = "mq.demo.work";

    public static final String EX_FANOUT = "mq.demo.fanout";
    public static final String Q_FANOUT_1 = "mq.demo.fanout.q1";
    public static final String Q_FANOUT_2 = "mq.demo.fanout.q2";

    public static final String EX_DIRECT = "mq.demo.direct";
    public static final String Q_DIRECT_ERROR = "mq.demo.direct.error";
    public static final String Q_DIRECT_INFO = "mq.demo.direct.info";
    public static final String RK_ERROR = "error";
    public static final String RK_INFO = "info";

    public static final String EX_TOPIC = "mq.demo.topic";
    public static final String Q_TOPIC_ORDER = "mq.demo.topic.order";
    public static final String Q_TOPIC_ALL = "mq.demo.topic.all";

    @Bean
    public Queue mqDemoSimpleQueue() {
        return new Queue(Q_SIMPLE, true);
    }

    @Bean
    public Queue mqDemoWorkQueue() {
        return new Queue(Q_WORK, true);
    }

    @Bean
    public FanoutExchange mqDemoFanoutExchange() {
        return new FanoutExchange(EX_FANOUT, true, false);
    }

    @Bean
    public Queue mqDemoFanoutQ1() {
        return new Queue(Q_FANOUT_1, true);
    }

    @Bean
    public Queue mqDemoFanoutQ2() {
        return new Queue(Q_FANOUT_2, true);
    }

    @Bean
    public Binding mqDemoFanoutBind1() {
        return BindingBuilder.bind(mqDemoFanoutQ1()).to(mqDemoFanoutExchange());
    }

    @Bean
    public Binding mqDemoFanoutBind2() {
        return BindingBuilder.bind(mqDemoFanoutQ2()).to(mqDemoFanoutExchange());
    }

    @Bean
    public DirectExchange mqDemoDirectExchange() {
        return new DirectExchange(EX_DIRECT, true, false);
    }

    @Bean
    public Queue mqDemoDirectErrorQueue() {
        return new Queue(Q_DIRECT_ERROR, true);
    }

    @Bean
    public Queue mqDemoDirectInfoQueue() {
        return new Queue(Q_DIRECT_INFO, true);
    }

    @Bean
    public Binding mqDemoDirectErrorBind() {
        return BindingBuilder.bind(mqDemoDirectErrorQueue()).to(mqDemoDirectExchange()).with(RK_ERROR);
    }

    @Bean
    public Binding mqDemoDirectInfoBind() {
        return BindingBuilder.bind(mqDemoDirectInfoQueue()).to(mqDemoDirectExchange()).with(RK_INFO);
    }

    @Bean
    public TopicExchange mqDemoTopicExchange() {
        return new TopicExchange(EX_TOPIC, true, false);
    }

    @Bean
    public Queue mqDemoTopicOrderQueue() {
        return new Queue(Q_TOPIC_ORDER, true);
    }

    @Bean
    public Queue mqDemoTopicAllQueue() {
        return new Queue(Q_TOPIC_ALL, true);
    }

    @Bean
    public Binding mqDemoTopicOrderBind() {
        return BindingBuilder.bind(mqDemoTopicOrderQueue()).to(mqDemoTopicExchange()).with("order.*");
    }

    @Bean
    public Binding mqDemoTopicAllBind() {
        return BindingBuilder.bind(mqDemoTopicAllQueue()).to(mqDemoTopicExchange()).with("#");
    }
}