#!/usr/bin/env bash
set -euo pipefail
NAME=smartadmin-mysql
NETWORK=smartadmin_default
VOLUME=smartadmin_mysql-data
PASS="${MYSQL_ROOT_PASSWORD:-root}"

case "${1:-}" in
  start)
    docker start "$NAME" 2>/dev/null || docker run -d \
      --name "$NAME" \
      --restart unless-stopped \
      --network "$NETWORK" \
      --network-alias mysql \
      --memory 800m \
      -e TZ=Asia/Shanghai \
      -e MYSQL_ROOT_PASSWORD="$PASS" \
      -e MYSQL_DATABASE=smart_admin_v3 \
      -p 127.0.0.1:3307:3306 \
      -v "$VOLUME:/var/lib/mysql" \
      mysql:8.0 \
      --character-set-server=utf8mb4 \
      --collation-server=utf8mb4_unicode_ci \
      --default-authentication-plugin=mysql_native_password
    ;;
  stop) docker stop "$NAME" ;;
  restart) docker restart "$NAME" ;;
  status) docker ps -a --filter name="$NAME" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" ;;
  *) echo "usage: $0 {start|stop|restart|status}"; exit 1 ;;
esac
