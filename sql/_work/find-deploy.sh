#!/bin/bash
set -e
docker inspect smartadmin-api --format 'Image={{.Config.Image}} Created={{.Created}}'
docker inspect smartadmin-api --format '{{json .Config.Labels}}' | tr ',' '\n' | grep -E 'working_dir|config_files|project' || true
echo "=== /data/smartadmin ==="
ls -la /data/smartadmin 2>/dev/null | head -25 || echo missing
echo "=== /home/ubuntu/apps/smart-admin ==="
ls -la /home/ubuntu/apps/smart-admin 2>/dev/null | head -25 || echo missing
echo "=== MallSchema on server trees ==="
find /data/smartadmin /home/ubuntu/apps/smart-admin -name 'MallSchemaService.java' 2>/dev/null | head
find /data/smartadmin /home/ubuntu/apps/smart-admin -path '*mall/service' -type d 2>/dev/null | head