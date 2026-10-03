package net.lab1024.sa.admin.module.business.mqdemo.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.mqdemo.service.MqDemoProducerService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq/demo")
@Tag(name = AdminSwaggerTagConst.Business.MQ_DEMO)
public class MqDemoController {

    @Resource
    private MqDemoProducerService mqDemoProducerService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @SaIgnore
    @GetMapping("/health")
    @Operation(summary = "health check rabbitmq")
    public ResponseDTO<String> health() {
        rabbitTemplate.execute(channel -> {
            channel.queueDeclarePassive("mq.demo.simple");
            return null;
        });
        return ResponseDTO.ok("rabbitmq-ok");
    }

    @SaIgnore
    @PostMapping("/simple")
    @Operation(summary = "1 simple queue")
    public ResponseDTO<String> simple(@RequestParam("msg") String msg) {
        mqDemoProducerService.sendSimple(msg);
        return ResponseDTO.ok("sent-simple");
    }

    @SaIgnore
    @PostMapping("/work")
    @Operation(summary = "2 work queue")
    public ResponseDTO<String> work(@RequestParam(defaultValue = "hello-work") String msg,
                                    @RequestParam(defaultValue = "5") int count) {
        mqDemoProducerService.sendWork(msg, Math.max(1, Math.min(count, 100000)));
        return ResponseDTO.ok("sent-work-" + count);
    }

    @SaIgnore
    @PostMapping("/fanout")
    @Operation(summary = "3 fanout pub-sub")
    public ResponseDTO<String> fanout(@RequestParam(defaultValue = "hello-fanout") String msg) {
        mqDemoProducerService.sendFanout(msg);
        return ResponseDTO.ok("sent-fanout");
    }

    @SaIgnore
    @PostMapping("/routing")
    @Operation(summary = "4 direct routing, key=error|info")
    public ResponseDTO<String> routing(@RequestParam(defaultValue = "error") String key,
                                       @RequestParam(defaultValue = "hello-routing") String msg) {
        mqDemoProducerService.sendRouting(key, msg);
        return ResponseDTO.ok("sent-routing-" + key);
    }

    @SaIgnore
    @PostMapping("/topic")
    @Operation(summary = "5 topic, e.g. order.created")
    public ResponseDTO<String> topic(@RequestParam(defaultValue = "order.created") String key,
                                     @RequestParam(defaultValue = "hello-topic") String msg) {
        mqDemoProducerService.sendTopic(key, msg);
        return ResponseDTO.ok("sent-topic-" + key);
    }
}