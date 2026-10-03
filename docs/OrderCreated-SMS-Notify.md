# OrderCreated SMS Notify (RabbitMQ + Tencent Cloud SMS)

## Overview

After mall seckill order create **commits**, publish `OrderCreated` to RabbitMQ.
Consumer sends real Tencent Cloud SMS. Idempotent by `orderNo` + channel `SMS`.
Uses DLX/DLQ and table `t_mall_order_notify`.

## Queue topology

| Resource | Name |
|----------|------|
| Exchange (topic) | `order.event` |
| Routing key | `order.created` |
| Queue (durable) | `order.notify.queue` |
| DLX | `order.event.dlx` |
| DLQ | `order.notify.dlq` |
| DLQ routing key | `order.notify.dlq` |

Listener: manual ACK (`spring.rabbitmq.listener.simple.acknowledge-mode: manual`).

## IDEA env vars (Run Configuration -> Environment variables)

```text
TENCENT_SECRET_ID=your_secret_id
TENCENT_SECRET_KEY=your_secret_key
TENCENT_SMS_SDK_APP_ID=your_sdk_app_id
TENCENT_SMS_SIGN_NAME=your_sign_name
TENCENT_SMS_TEMPLATE_ID=your_template_id
```

Optional local file (gitignored): `tencent-sms.local.env` — do not commit.

YAML (`sa-base.yaml` in dev/prod/pre/test) uses placeholders only:

```yaml
tencent:
  sms:
    enabled: true
    secret-id: ${TENCENT_SECRET_ID:}
    secret-key: ${TENCENT_SECRET_KEY:}
    sdk-app-id: ${TENCENT_SMS_SDK_APP_ID:}
    sign-name: ${TENCENT_SMS_SIGN_NAME:}
    template-id: ${TENCENT_SMS_TEMPLATE_ID:}
    region: ap-guangzhou
```

## Tencent console links

- SMS console: https://console.cloud.tencent.com/smsv2
- Application management (SdkAppId): https://console.cloud.tencent.com/smsv2/app-manage
- Signatures: https://console.cloud.tencent.com/smsv2/csms-sign
- Templates: https://console.cloud.tencent.com/smsv2/csms-template
- API docs SendSms: https://cloud.tencent.com/document/product/382/55981

Template params expected by code: `[orderNo, amount]` (2 params).

## SQL

Apply `sql/t_mall_order_notify.sql` on MySQL database `smart_admin_v3` (or your mall DB name).

Server apply example (from Windows, do not print password):

```powershell
$pem = "C:\Users\王永雁\Downloads\SSH_123123.pem"
$sql = Get-Content -Raw "D:\Personal\project\smartadmin\sql\t_mall_order_notify.sql"
ssh -i $pem ubuntu@175.27.131.7 @'
set -e
cd /home/ubuntu/apps/smart-admin/deploy
set -a; . ./.env; set +a
docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" -i smartadmin-mysql \
  mysql -uroot smart_admin_v3
'@ <<< "$sql"
```

(Adjust heredoc / piping for your SSH client; prefer `Get-Content sql | ssh ... 'docker exec -i ...'`.)

## Acceptance tests

1. Create seckill order -> returns orderNo; row in `t_mall_order_notify` with `status=SUCCESS`, channel=SMS.
2. Stop consumer / app listener, create order -> order still succeeds; start listener -> notify row appears (async catch-up).
3. Re-deliver same `OrderCreated` (or replay message) -> still one SUCCESS row; SMS not sent again.
4. Force SMS failure (bad template id) -> `retry_count` increases; after >=3, message goes to `order.notify.dlq`.
5. Blank mobile / blank orderNo -> nack requeue=false -> DLQ (non-recoverable).
6. RabbitMQ management http://175.27.131.7:15672 shows `order.event`, `order.notify.queue`, `order.notify.dlq`.

## Code map

- `.../mall/notify/config/OrderNotifyRabbitConfig.java`
- `.../mall/notify/config/TencentSmsProperties.java`
- `.../mall/notify/domain/OrderCreatedEvent.java`
- `.../mall/notify/domain/entity/MallOrderNotifyEntity.java`
- `.../mall/notify/dao/MallOrderNotifyDao.java`
- `.../mall/notify/service/MallOrderNotifyService.java`
- `.../mall/notify/producer/OrderCreatedEventPublisher.java`
- `.../mall/notify/sms/TencentSmsClient.java`
- `.../mall/notify/listener/OrderNotifyListener.java`
- Wire: `MallOrderService.create` afterCommit -> `publisher.publishAfterCommit(...)` (see snippets)

## Notes

- Secrets stay as env placeholders; never hardcode in yaml/git.
- If `SdkAppId` / sign / template missing, `TencentSmsClient` throws clear `IllegalStateException`.
- Do not push git unless asked.

## Personal / no qualification

Default 	encent.sms.mock: true. Logs MOCK SMS and writes notify SUCCESS. Set mock=false and fill SdkAppId/Sign/Template when enterprise SMS is ready.
