#!/bin/bash
set -e
set -a
. /data/smartadmin/deploy/.env
set +a
runsql() { docker exec -i smartadmin-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" smart_admin_v3 -e "$1"; }
runsql "ALTER TABLE t_seckill_activity ADD COLUMN commission_rate_l2 decimal(6,4) DEFAULT NULL COMMENT 'l2';" || true
runsql "ALTER TABLE t_mall_member ADD COLUMN commission_level int DEFAULT NULL COMMENT 'level';" || true
echo COLS_ACTIVITY
runsql "SHOW COLUMNS FROM t_seckill_activity LIKE 'commission%';"
echo COLS_MEMBER
runsql "SHOW COLUMNS FROM t_mall_member LIKE 'commission%';"
chmod -R a+rX /data/smartadmin/smart-app/dist/build/h5
ls -ld /data/smartadmin/smart-app/dist/build/h5/static
echo FIX_DONE
