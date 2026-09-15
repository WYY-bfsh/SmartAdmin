package net.lab1024.sa.admin.module.business.mqdemo.service;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.mqdemo.config.MqDemoRabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MqDemoProducerService {

    private static final Logger log = LoggerFactory.getLogger(MqDemoProducerService.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendSimple(String msg) {
        rabbitTemplate.convertAndSend(MqDemoRabbitConfig.Q_SIMPLE, msg);
        log.info("[mq-demo][simple] send={}", msg);
    }

    public void sendWork(String msg, int count) {
        for (int i = 1; i <= count; i++) {
            String body = msg + " #" + i;
            rabbitTemplate.convertAndSend(MqDemoRabbitConfig.Q_WORK, body);
            log.info("[mq-demo][work] send={}", body);
        }
    }

    public void sendFanout(String msg) {
        rabbitTemplate.convertAndSend(MqDemoRabbitConfig.EX_FANOUT, "", msg);
        log.info("[mq-demo][fanout] send={}", msg);
    }

    public void sendRouting(String routingKey, String msg) {
        rabbitTemplate.convertAndSend(MqDemoRabbitConfig.EX_DIRECT, routingKey, msg);
        log.info("[mq-demo][routing] key={} send={}", routingKey, msg);
    }

    public void sendTopic(String routingKey, String msg) {
        rabbitTemplate.convertAndSend(MqDemoRabbitConfig.EX_TOPIC, routingKey, msg);
        log.info("[mq-demo][topic] key={} send={}", routingKey, msg);
    }
}