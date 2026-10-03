#!/bin/bash
set -euo pipefail
echo "=== wait api ready ==="
for i in $(seq 1 40); do
  if docker logs smartadmin-api 2>&1 | grep -q "Started AdminApplication"; then
    echo "STARTED at try=$i"
    break
  fi
  if docker logs smartadmin-api 2>&1 | grep -qiE "Application run failed|IllegalArgumentException"; then
    echo "FAIL_START"
    docker logs smartadmin-api 2>&1 | tail -80
    exit 1
  fi
  sleep 3
done
docker logs smartadmin-api 2>&1 | tail -40
echo "=== health curl via nginx 8081 ==="
curl -sS -o /tmp/captcha.json -w "captcha_http=%{http_code}\n" http://127.0.0.1:8081/api/login/getCaptcha
head -c 200 /tmp/captcha.json; echo
curl -sS -o /dev/null -w "root_http=%{http_code}\n" http://127.0.0.1:8081/
curl -sS -o /dev/null -w "app_http=%{http_code}\n" http://127.0.0.1:8081/app/
echo "=== schema service in image? ==="
docker exec smartadmin-api sh -c 'jar tf /app/app.jar | grep -i SchemaService || echo NO_SchemaService_IN_JAR'
echo "=== ports unchanged ==="
docker ps --format '{{.Names}} {{.Ports}}' | grep smartadmin-nginx