#!/bin/bash
set -e
cd /data/smartadmin/deploy
grep -n allowPublicKeyRetrieval docker-compose.yml
docker compose -p smartadmin up -d --force-recreate --no-deps sa-admin
docker restart smartadmin-nginx
echo RECREATED
