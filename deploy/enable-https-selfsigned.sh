#!/bin/bash
# 用自签证书开 HTTPS。有域名后证书 SAN 含 desire.wang。
# 浏览器会提示「不安全」，这是自签证书的正常现象。正式证书再用 Let's Encrypt。
# 在服务器上执行：bash /data/smartadmin/deploy/enable-https-selfsigned.sh
# 禁止 docker compose down -v
set -euo pipefail

ROOT=/data/smartadmin
DOMAIN=desire.wang
IP=175.27.131.7
CERT_DIR="$ROOT/deploy/nginx/certs"
TEMPLATE="$ROOT/deploy/nginx/ssl/smartadmin-ssl.conf.template"
SSL_CONF="$ROOT/deploy/nginx/conf.d/smartadmin-ssl.conf"

test -f "$TEMPLATE" || { echo "MISSING $TEMPLATE  先把本仓库最新 deploy/ 同步到服务器"; exit 1; }
test -f "$ROOT/deploy/docker-compose.yml" || { echo "MISSING compose"; exit 1; }

mkdir -p "$CERT_DIR"

need_cert=1
if [ -f "$CERT_DIR/selfsigned.crt" ] && [ -f "$CERT_DIR/selfsigned.key" ]; then
  if openssl x509 -in "$CERT_DIR/selfsigned.crt" -noout -text 2>/dev/null | grep -q "$DOMAIN"; then
    need_cert=0
    echo "=== 已有含 $DOMAIN 的证书，跳过生成 ==="
  fi
fi

if [ "$need_cert" -eq 1 ]; then
  echo "=== 生成自签证书（825 天，SAN=DNS:$DOMAIN + IP:$IP）==="
  openssl req -x509 -nodes -days 825 -newkey rsa:2048 \
    -keyout "$CERT_DIR/selfsigned.key" \
    -out "$CERT_DIR/selfsigned.crt" \
    -subj "/CN=$DOMAIN" \
    -addext "subjectAltName=DNS:$DOMAIN,DNS:www.$DOMAIN,IP:$IP"
  chmod 600 "$CERT_DIR/selfsigned.key"
fi

sed "s/__SERVER_NAME__/${DOMAIN}/g" "$TEMPLATE" > "$SSL_CONF"
echo "=== 已写入 $SSL_CONF ==="

cd "$ROOT/deploy"
set -a
# shellcheck disable=SC1091
. ./.env
set +a

docker compose -p smartadmin up -d nginx
docker exec smartadmin-nginx nginx -t
docker restart smartadmin-nginx

echo
echo "=== 本机验收 ==="
curl -sS -o /dev/null -w "http80   %{http_code}\n" http://127.0.0.1/ || true
curl -sS -o /dev/null -w "http8080 %{http_code}\n" http://127.0.0.1:8080/ || true
curl -sk -o /dev/null -w "https443 %{http_code}\n" https://127.0.0.1/ || true

echo
echo "外网请打开：https://$DOMAIN/"
echo "DNS 的 A 记录需指向 $IP，安全组放行 80 和 443。"
echo "自签证书浏览器会警告：高级 → 继续访问。"
echo "旧地址 http://$IP:8080/ 仍然可用。"
echo
echo "HTTPS_SELFSIGNED_DONE"
