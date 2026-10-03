package net.lab1024.sa.admin.module.business.mqdemo.listener;

import com.rabbitmq.client.Channel;
import net.lab1024.sa.admin.module.business.mqdemo.config.MqDemoRabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MqDemoListeners {

    private static final Logger log = LoggerFactory.getLogger(MqDemoListeners.class);

    @RabbitListener(queues = MqDemoRabbitConfig.Q_SIMPLE)
    public void onSimple(String body, Message message, Channel channel) throws IOException {
        try {
            log.info("[mq-demo][simple] recv={}", body);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            throw e;
        }
    }
    // 此处是消费者模式 ， 监听的时候 设置 concurrency=‘5’ 意思是2个线程在消费 代理 broker 里面的 消息
    @RabbitListener(queues = MqDemoRabbitConfig.Q_WORK, concurrency = "2-2")
    public void onWork(String body, Message message, Channel channel) throws Exception {
        try {
            log.info("[mq-demo][work] thread={} recv={}", Thread.currentThread().getName(), body);
//            Thread.sleep(300);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            throw e;
        }
    }

    @RabbitListener(queues = MqDemoRabbitConfig.Q_FANOUT_1)
    public void onFanout1(String body, Message message, Channel channel) throws IOException {
        log.info("[mq-demo][fanout-q1] recv={}", body);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = MqDemoRabbitConfig.Q_FANOUT_2)
    public void onFanout2(String body, Message message, Channel channel) throws IOException {
        log.info("[mq-demo][fanout-q2] recv={}", body);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = MqDemoRabbitConfig.Q_DIRECT_ERROR)
    public void onDirectError(String body, Message message, Channel channel) throws IOException {
        log.info("[mq-demo][routing-error] recv={}", body);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = MqDemoRabbitConfig.Q_DIRECT_INFO)
    public void onDirectInfo(String body, Message message, Channel channel) throws IOException {
        log.info("[mq-demo][routing-info] recv={}", body);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = MqDemoRabbitConfig.Q_TOPIC_ORDER)
    public void onTopicOrder(String body, Message message, Channel channel) throws IOException {
        log.info("[mq-demo][topic-order] recv={}", body);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = MqDemoRabbitConfig.Q_TOPIC_ALL)
    public void onTopicAll(String body, Message message, Channel channel) throws IOException {
        log.info("[mq-demo][topic-all] recv={}", body);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}