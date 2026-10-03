# Docker + WireGuard VPN (MySQL / Redis)

Server: 175.27.131.7 | Project: smartadmin | Updated: 2026-09-15

## 1. Idea

Docker does NOT run a VPN plugin. Two layers work together:

1. WireGuard creates NIC `wg0` on server = `10.66.66.1/24`; your PC = `10.66.66.2`.
2. Docker Compose `ports` bind container ports to `10.66.66.1` (and `127.0.0.1`), NOT `0.0.0.0`.
3. With WireGuard Active, open `10.66.66.1:3306` / `:6379`.

```text
PC 10.66.66.2
   |  WireGuard UDP 51820
   v
Server wg0 10.66.66.1
   |  Docker publish
   +--> MySQL :3306
   +--> Redis :6379
```

## 2. Server WireGuard

```bash
sudo apt-get install -y wireguard wireguard-tools
sudo systemctl enable --now wg-quick@wg0
sudo wg show
ip -4 addr show wg0
```

Files under `/etc/wireguard/`:
- `wg0.conf` (server)
- `smartadmin-vpn.conf` (client import)
- `server_*.key` / `client_*.key`

Server `wg0.conf`:

```ini
[Interface]
Address = 10.66.66.1/24
ListenPort = 51820
PrivateKey = <SERVER_PRIVATE_KEY>

[Peer]
PublicKey = <CLIENT_PUBLIC_KEY>
AllowedIPs = 10.66.66.2/32
```

Client:

```ini
[Interface]
PrivateKey = <CLIENT_PRIVATE_KEY>
Address = 10.66.66.2/24
DNS = 1.1.1.1

[Peer]
PublicKey = <SERVER_PUBLIC_KEY>
Endpoint = 175.27.131.7:51820
AllowedIPs = 10.66.66.0/24
PersistentKeepalive = 25
```

Tencent security group: allow **UDP 51820**. Do NOT open public TCP 3306/6379.

Local import file: `D:\Personal\WireGuard\smartadmin-vpn.conf`  
Check: `ping 10.66.66.1`

## 3. How Docker maps to VPN

Key is Compose `ports` host bind IP:

```yaml
# /home/ubuntu/apps/smart-admin/deploy/docker-compose.yml
services:
  mysql:
    ports:
      - "127.0.0.1:3306:3306"     # server local / SSH tunnel only
      - "10.66.66.1:3306:3306"    # WireGuard clients only
  redis:
    ports:
      - "127.0.0.1:6379:6379"
      - "10.66.66.1:6379:6379"
```

Meaning: `"HOST_IP:HOST_PORT:CONTAINER_PORT"`

| HOST_IP | Who can connect |
|---|---|
| `10.66.66.1` | WireGuard peers |
| `127.0.0.1` | Server itself |
| `0.0.0.0` / omitted | Public Internet (avoid for DB) |

Apply:

```bash
cd /home/ubuntu/apps/smart-admin/deploy
docker compose -p smartadmin up -d mysql redis
ss -lnt | grep -E ':3306|:6379'
```

Containers stay on Docker bridge `smartadmin_default`. No need to attach `wg0` into Docker network / macvlan.

## 4. Local app / Navicat

| Use | Host | Port | Auth |
|---|---|---|---|
| MySQL | `10.66.66.1` | 3306 | `smart_admin_v3` / `root` |
| Redis | `10.66.66.1` | 6379 | no password (leave empty) |
| RabbitMQ | `175.27.131.7` | 5672 | `admin` / `admin123` |

`dev/sa-base.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:p6spy:mysql://10.66.66.1:3306/smart_admin_v3?...
    username: smart_admin_v3
    password: root
  data:
    redis:
      database: 1
      host: 10.66.66.1
      port: 6379
      password:
```

Do NOT use:
- `127.0.0.1` (your PC, not cloud DB)
- `175.27.131.7:3306/6379` (not published)
- `10.66.66.1` without WireGuard Active

## 5. Troubleshoot

| Symptom | Check |
|---|---|
| ping 10.66.66.1 fail | Tunnel Active? UDP 51820? `sudo wg show` handshake |
| ping OK, MySQL deny | `ss -lnt | grep 3306` has 10.66.66.1? user/pass |
| Redis AUTH fail | server has no requirepass; clear local password |
| `@localhost` deny | you hit local 127.0.0.1; switch to 10.66.66.1 |

## 6. Paths

| Item | Path |
|---|---|
| Compose | `/home/ubuntu/apps/smart-admin/deploy/docker-compose.yml` |
| WG server | `/etc/wireguard/wg0.conf` |
| Client conf | `D:\Personal\WireGuard\smartadmin-vpn.conf` |
| Spring | `smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml` |

---

One line: Docker does not speak WireGuard; it only publishes ports on the VPN NIC IP `10.66.66.1`.