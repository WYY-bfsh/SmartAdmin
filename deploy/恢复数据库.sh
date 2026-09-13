#!/usr/bin/env bash
# 在服务器执行：把被清空的库按官方脚本 + 本项目扩展表重建。
# 用法：
#   cd /data/smartadmin
#   bash deploy/恢复数据库.sh
#
# 密码来源（按顺序）：
#   1) 环境变量 MYSQL_ROOT_PASSWORD
#   2) deploy/.env 里的 MYSQL_ROOT_PASSWORD
#   3) 临时用法：MYSQL_ROOT_PASSWORD=你的密码 bash deploy/恢复数据库.sh

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"

# ---------- 密码 ----------
if [ -z "${MYSQL_ROOT_PASSWORD:-}" ] && [ -f "$HERE/.env" ]; then
  MYSQL_ROOT_PASSWORD="$(grep -E '^MYSQL_ROOT_PASSWORD=' "$HERE/.env" | tail -1 | cut -d= -f2- || true)"
fi
if [ -z "${MYSQL_ROOT_PASSWORD:-}" ]; then
  echo "缺少库密码：请在 $HERE/.env 写 MYSQL_ROOT_PASSWORD=xxx"
  echo "或临时：MYSQL_ROOT_PASSWORD=xxx bash $0"
  exit 1
fi
PASS="$MYSQL_ROOT_PASSWORD"

# ---------- 官方主脚本 smart_admin_v3.sql ----------
SQL_DIR=""
for d in "$ROOT/sql" "$ROOT/sql-src" "$ROOT/数据库SQL脚本/mysql" \
         "/data/smartadmin/sql" "/data/smartadmin/sql-src"
do
  if [ -f "$d/smart_admin_v3.sql" ]; then
    SQL_DIR="$d"
    break
  fi
done
if [ -z "$SQL_DIR" ]; then
  echo "找不到 smart_admin_v3.sql。找过：$ROOT/sql、$ROOT/sql-src、$ROOT/数据库SQL脚本/mysql"
  echo "把官方脚本放到 $ROOT/sql/smart_admin_v3.sql 后重试"
  exit 1
fi

# ---------- 本项目扩展脚本（不在 sql/ 里，单独探测）----------
MODULES=""
for f in "$SQL_DIR/restore-project-modules.sql" \
         "$ROOT/restore-project-modules.sql" \
         "/data/smartadmin/restore-project-modules.sql" \
         "$ROOT/数据库SQL脚本/mysql/restore-project-modules.sql"
do
  if [ -f "$f" ]; then
    MODULES="$f"
    break
  fi
done
if [ -z "$MODULES" ]; then
  echo "找不到 restore-project-modules.sql"
  echo "找过：$SQL_DIR/、$ROOT/、/data/smartadmin/"
  echo "正常部署时它在 /data/smartadmin/restore-project-modules.sql（故意不放 sql/，避免首次 init 乱序）"
  exit 1
fi

# ---------- 前置检查 ----------
if ! docker ps --format '{{.Names}}' | grep -q '^smartadmin-mysql$'; then
  echo "容器 smartadmin-mysql 未运行。先：cd $HERE && docker compose -p smartadmin up -d mysql"
  exit 1
fi

echo "将使用："
echo "  官方脚本   $SQL_DIR/smart_admin_v3.sql"
echo "  扩展脚本   $MODULES"
echo ""

echo "1/3 导入官方库 smart_admin_v3.sql（会 DROP 并重建库，原数据清掉）"
docker exec -i smartadmin-mysql mysql -uroot -p"$PASS" < "$SQL_DIR/smart_admin_v3.sql"

echo "2/3 导入 Web/H5 全部自建模块（表 + 菜单 + 超管权限）"
docker exec -i smartadmin-mysql mysql -uroot -p"$PASS" smart_admin_v3 < "$MODULES"

echo "3/3 重启后端"
if docker ps --format '{{.Names}}' | grep -q '^smartadmin-api$'; then
  docker restart smartadmin-api
fi

echo
echo "完成。核对结果："
TABLES=$(docker exec smartadmin-mysql mysql -uroot -p"$PASS" -N -e \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='smart_admin_v3';" 2>/dev/null | tr -d '\r')
echo "  表数量           $TABLES   （含官方库约 50 + 本项目 21，低于 60 就是扩展脚本没进去）"
MENUS=$(docker exec smartadmin-mysql mysql -uroot -p"$PASS" -N -e \
  "SELECT COUNT(*) FROM smart_admin_v3.t_menu;" 2>/dev/null | tr -d '\r')
echo "  菜单 t_menu 行数  $MENUS   （应 >= 40）"
echo "默认后台账号仍是 admin / 123456，登录后请立刻改密码。"
