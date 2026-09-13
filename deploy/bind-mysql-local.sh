#!/bin/bash
set -e
cd /data/smartadmin/deploy
set -a
. ./.env
set +a
docker compose -p smartadmin up -d mysql
sleep 3
ss -lntn | grep 3306 || true
docker compose -p smartadmin ps mysql
docker exec smartadmin-mysql mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent && echo MYSQL_PING_OK
