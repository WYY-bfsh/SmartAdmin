# RabbitMQ 五种 AMQP 模式练习 (SmartAdmin 本地)

## 1. 服务器连接

| Item | Value |
|---|---|
| Host | `175.27.131.7` |
| AMQP | `5672` |
| Management | http://175.27.131.7:15672 |
| User/Pass | `admin` / `admin123` |
| VHost | `/` |
| Server dir | `/data/rabbitmq` |

腾讯云安全组: TCP 5672 / 15672

## 2. 本项目改动

- `sa-base/pom.xml` + spring-boot-starter-amqp
- `sa-base.yaml` (dev/prod/pre/test) spring.rabbitmq -> 175.27.131.7
- Swagger: `消息队列`
- Code: `module/business/mqdemo/**` (业务分类 business)

## 3. 五种模式

| Mode | API | Queue/Exchange | Check |
|---|---|---|---|
| 1 simple | POST /mq/demo/simple | mq.demo.simple | log [mq-demo][simple] recv= |
| 2 work | POST /mq/demo/work?count=5 | mq.demo.work concurrency=2 | different threads |
| 3 fanout | POST /mq/demo/fanout | mq.demo.fanout -> q1/q2 | both queues recv |
| 4 routing | POST /mq/demo/routing?key=error|info | mq.demo.direct | route by key |
| 5 topic | POST /mq/demo/topic?key=order.created | mq.demo.topic | order.* and # |

## 4. 启动与测试

1. RabbitMQ container up
2. Start sa-admin with **dev** (port 1024)
3. Knife4j category: **消息队列**
4. curl:

```bash
curl -X GET  "http://127.0.0.1:1024/mq/demo/health"
curl -X POST "http://127.0.0.1:1024/mq/demo/simple?msg=hello-simple"
curl -X POST "http://127.0.0.1:1024/mq/demo/work?msg=hello-work&count=5"
curl -X POST "http://127.0.0.1:1024/mq/demo/fanout?msg=hello-fanout"
curl -X POST "http://127.0.0.1:1024/mq/demo/routing?key=error&msg=err-1"
curl -X POST "http://127.0.0.1:1024/mq/demo/routing?key=info&msg=info-1"
curl -X POST "http://127.0.0.1:1024/mq/demo/topic?key=order.created&msg=order-1"
curl -X POST "http://127.0.0.1:1024/mq/demo/topic?key=user.login&msg=user-1"
```

APIs use @SaIgnore. Check app logs or management UI Queues.

## 5. 测试顺序

health -> simple -> work -> fanout -> routing -> topic

## 6. 注意

- Practice credentials only; do not send secrets/pay data.
- After password change, update sa-base.yaml.
- Production: separate user/vhost from practice.
