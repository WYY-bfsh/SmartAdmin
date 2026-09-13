#!/bin/bash
set -e
export DEBIAN_FRONTEND=noninteractive
sudo apt-get update -qq
sudo apt-get install -y wireguard wireguard-tools
sudo mkdir -p /etc/wireguard
CLIENT_PRIV='MJVKae8WZQXSHLOF0bcONIvFawAOPoWh/olZuJ9qnVo='
CLIENT_PUB=$(printf '%s\n' "$CLIENT_PRIV" | wg pubkey)
umask 077
tmp=$(mktemp -d)
wg genkey | tee "$tmp/server.key" | wg pubkey > "$tmp/server.pub"
sudo mv "$tmp/server.key" "$tmp/server.pub" /etc/wireguard/
rmdir "$tmp"
SERVER_PRIV=$(sudo cat /etc/wireguard/server.key)
SERVER_PUB=$(sudo cat /etc/wireguard/server.pub)
sudo tee /etc/wireguard/wg0.conf >/dev/null <<EOF
[Interface]
Address = 10.66.66.1/24
ListenPort = 51820
PrivateKey = ${SERVER_PRIV}

[Peer]
PublicKey = ${CLIENT_PUB}
AllowedIPs = 10.66.66.2/32
EOF
sudo chmod 600 /etc/wireguard/wg0.conf /etc/wireguard/server.key
echo 'net.ipv4.ip_forward=1' | sudo tee /etc/sysctl.d/99-wireguard.conf >/dev/null
sudo sysctl -w net.ipv4.ip_forward=1
sudo systemctl enable wg-quick@wg0
sudo systemctl restart wg-quick@wg0
sudo wg show
printf '%s\n' "$SERVER_PUB" > /tmp/wg-server.pub
echo WG_UP
echo SERVER_PUB="$SERVER_PUB"
