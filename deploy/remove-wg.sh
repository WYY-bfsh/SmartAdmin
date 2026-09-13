#!/bin/bash
set -e
sudo systemctl stop wg-quick@wg0 2>/dev/null || true
sudo systemctl disable wg-quick@wg0 2>/dev/null || true
sudo rm -f /etc/wireguard/wg0.conf /etc/wireguard/server.key /etc/wireguard/server.pub /etc/wireguard/client.key /etc/wireguard/client.pub
sudo rm -f /etc/sysctl.d/99-wireguard.conf
sudo apt-get remove -y wireguard wireguard-tools
sudo rm -rf /tmp/setup-wg.sh /tmp/smartadmin-vpn.conf /tmp/bind-mysql-wg.sh
echo WG_REMOVED
ip link show wg0 2>&1 || true
