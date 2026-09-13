#!/bin/bash
set -e
cd /data/smartadmin/deploy
set -a
. ./.env
set +a
docker compose -p smartadmin up -d mysql
ss -lntn | grep 3306 || true
echo BIND_OK
