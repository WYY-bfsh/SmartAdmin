#!/bin/bash
set -euo pipefail
ENVF=/home/ubuntu/apps/smart-admin/deploy/.env
set -a
. "$ENVF"
set +a
CNAME=$(docker ps --format '{{.Names}}' | grep -E 'mysql|mariadb' | head -1)
TABLES=$(docker exec "$CNAME" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='smart_admin_v3' AND (TABLE_NAME LIKE 't_mall%' OR TABLE_NAME LIKE 't_seckill%' OR TABLE_NAME LIKE 't_pay%' OR TABLE_NAME LIKE 't_media%') ORDER BY 1;")
echo "dumping: $TABLES"
docker exec "$CNAME" mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --no-data --skip-comments --set-gtid-purged=OFF --default-character-set=utf8mb4 smart_admin_v3 $TABLES > /tmp/online-business-schema-dump.sql
# also with data? user said 导入 sql - schema dump is enough for formal; add row counts
docker exec "$CNAME" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e "
SELECT CONCAT(TABLE_NAME, '=', TABLE_ROWS) FROM information_schema.TABLES
WHERE TABLE_SCHEMA='smart_admin_v3'
  AND (TABLE_NAME LIKE 't_mall%' OR TABLE_NAME LIKE 't_seckill%' OR TABLE_NAME LIKE 't_pay%' OR TABLE_NAME LIKE 't_media%')
ORDER BY 1;
" > /tmp/online-business-table-rows.txt
wc -c /tmp/online-business-schema-dump.sql
head -5 /tmp/online-business-schema-dump.sql