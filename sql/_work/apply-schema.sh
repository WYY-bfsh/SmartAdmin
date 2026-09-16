#!/bin/bash
set -euo pipefail
ENVF=/home/ubuntu/apps/smart-admin/deploy/.env
set -a
. "$ENVF"
set +a
CNAME=$(docker ps --format '{{.Names}}' | grep -E 'mysql|mariadb' | head -1)
echo "container=$CNAME"
docker exec -i "$CNAME" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 < /tmp/formal-business-schema.sql
echo "APPLY_OK"
# verify key columns
docker exec "$CNAME" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e "
SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='smart_admin_v3'
  AND (
    (TABLE_NAME='t_mall_member' AND COLUMN_NAME IN ('wechat_openid','commission_level','wechat_pay_qr'))
 OR (TABLE_NAME='t_mall_order' AND COLUMN_NAME IN ('pay_channel','wx_transaction_id','pay_proof_url'))
 OR (TABLE_NAME='t_seckill_activity' AND COLUMN_NAME='commission_rate_l2')
 OR (TABLE_NAME='t_mall_commission' AND COLUMN_NAME='commission_level')
 OR (TABLE_NAME='t_pay_order' AND COLUMN_NAME IN ('mall_order_id','pay_channel'))
 OR (TABLE_NAME='t_mall_order_notify' AND COLUMN_NAME='order_no')
  )
ORDER BY 1,2;
"