#!/bin/bash
set -e
set -a
. /data/smartadmin/deploy/.env
set +a
OLD="$MYSQL_ROOT_PASSWORD"
NEW="smart_admin_v3"
docker exec -i smartadmin-mysql mysql -uroot -p"$OLD" <<SQL
ALTER USER 'root'@'%' IDENTIFIED BY '${NEW}';
ALTER USER 'root'@'localhost' IDENTIFIED BY '${NEW}';
FLUSH PRIVILEGES;
SELECT user, host FROM mysql.user WHERE user='root';
SQL
printf 'MYSQL_ROOT_PASSWORD=%s\n' "$NEW" > /data/smartadmin/deploy/.env
echo ENV_WRITTEN
cd /data/smartadmin/deploy
docker compose -p smartadmin up -d sa-admin mysql
sleep 4
docker exec -i smartadmin-mysql mysql -uroot -p"$NEW" -e "SELECT 1 AS ok;"
echo MYSQL_NEW_OK
