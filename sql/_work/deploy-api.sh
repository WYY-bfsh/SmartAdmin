#!/bin/bash
set -euo pipefail
API=/home/ubuntu/apps/smart-admin/smart-admin-api-java17-springboot3
DEPLOY=/home/ubuntu/apps/smart-admin/deploy

echo "=== backup api tree timestamp ==="
ts=$(date +%Y%m%d%H%M%S)
# light backup of mall/pay/media service dirs only
mkdir -p /tmp/smartadmin-api-bak-$ts
cp -a "$API/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/mall/service" "/tmp/smartadmin-api-bak-$ts/mall-service" 2>/dev/null || true

echo "=== extract new source (keep nothing under API, replace tree) ==="
# replace contents but keep directory
find "$API" -mindepth 1 -maxdepth 1 ! -name 'target' -exec rm -rf {} +
mkdir -p "$API"
tar -xzf /tmp/smartadmin-api-src.tgz -C "$API"
# confirm SchemaService gone
if find "$API" -name '*SchemaService.java' | grep -q .; then
  echo "FAIL: SchemaService still present"
  find "$API" -name '*SchemaService.java'
  exit 1
fi
echo "SchemaService removed on server: OK"
ls "$API/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/mall/service" | head

echo "=== docker build sa-admin (ports unchanged) ==="
cd "$DEPLOY"
# show current nginx ports before/after - do not edit compose ports
grep -n '8080\|8081\|ports:' docker-compose.yml | head -40
docker compose -p smartadmin build sa-admin
echo "BUILD_OK"

echo "=== recreate api only ==="
docker compose -p smartadmin up -d --force-recreate --no-deps sa-admin
docker restart smartadmin-nginx
sleep 8
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' | grep -E 'smartadmin|NAMES'
echo "RECREATE_OK"