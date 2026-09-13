#!/bin/bash
# 为 desire.wang 申请 Let's Encrypt 证书（去掉浏览器不受信任警告）
# 在服务器执行：bash /data/smartadmin/deploy/enable-https-letsencrypt.sh
# 禁止 docker compose down -v
set -euo pipefail

ROOT=/data/smartadmin
DOMAIN=desire.wang
WWW=www.desire.wang
COMPOSE_DIR="$ROOT/deploy"
WEBROOT="$COMPOSE_DIR/nginx/certbot/www"
LE_DIR="$COMPOSE_DIR/nginx/certbot/conf"
TEMPLATE="$COMPOSE_DIR/nginx/ssl/smartadmin-ssl-letsencrypt.conf.template"
SSL_CONF="$COMPOSE_DIR/nginx/conf.d/smartadmin-ssl.conf"

test -f "$TEMPLATE"
mkdir -p "$WEBROOT" "$LE_DIR"

cd "$COMPOSE_DIR"
set -a
# shellcheck disable=SC1091
. ./.env
set +a

docker compose -p smartadmin up -d nginx
sleep 2
docker exec smartadmin-nginx nginx -t
docker exec smartadmin-nginx nginx -s reload || docker restart smartadmin-nginx

mkdir -p "$WEBROOT/.well-known/acme-challenge"
echo ok > "$WEBROOT/.well-known/acme-challenge/ping"
# 卷是 :ro，写入发生在宿主机；容器应能读到
curl -sS -m 8 "http://127.0.0.1/.well-known/acme-challenge/ping" | grep -q ok \
  || { echo "ACME 目录未被 Nginx 读到"; exit 1; }

CERT_ARGS=(-d "$DOMAIN")
if getent hosts "$WWW" >/dev/null 2>&1; then
  CERT_ARGS+=(-d "$WWW")
fi

echo "=== 申请 Let's Encrypt：${CERT_ARGS[*]} ==="
docker run --rm \
  -v "$WEBROOT:/var/www/certbot" \
  -v "$LE_DIR:/etc/letsencrypt" \
  certbot/certbot certonly --webroot -w /var/www/certbot \
  "${CERT_ARGS[@]}" \
  --agree-tos --register-unsafely-without-email --non-interactive --keep-until-expiring

sudo test -f "$LE_DIR/live/$DOMAIN/fullchain.pem"
sudo test -f "$LE_DIR/live/$DOMAIN/privkey.pem"
sudo chmod -R a+rX "$LE_DIR/live" "$LE_DIR/archive"

sed "s/__SERVER_NAME__/${DOMAIN}/g" "$TEMPLATE" > "$SSL_CONF"
docker compose -p smartadmin up -d nginx
docker exec smartadmin-nginx nginx -t
docker restart smartadmin-nginx

# 按用户要求：到期不自动续期。当前证书继续用到失效日。

echo
echo "=== 验收 ==="
echo | openssl s_client -connect 127.0.0.1:443 -servername "$DOMAIN" 2>/dev/null | openssl x509 -noout -issuer -subject -dates || true
curl -sS -o /dev/null -w "https_verify %{http_code}\n" --connect-timeout 8 "https://$DOMAIN/" || true
echo
echo "浏览器打开 https://$DOMAIN/ 应不再提示证书不受信任。"
echo "LETSENCRYPT_DONE"
