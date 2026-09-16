#!/bin/bash
set -e
ENVF=/home/ubuntu/apps/smart-admin/deploy/.env
set -a
. "$ENVF"
set +a
CNAME=$(docker ps --format '{{.Names}}' | grep -E 'mysql|mariadb' | head -1)
echo "container=$CNAME"
docker exec "$CNAME" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='smart_admin_v3' AND (TABLE_NAME LIKE 't_mall%' OR TABLE_NAME LIKE 't_seckill%' OR TABLE_NAME LIKE 't_pay%' OR TABLE_NAME LIKE 't_media%') ORDER BY 1;"