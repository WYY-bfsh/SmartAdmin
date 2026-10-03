#!/bin/bash
set -euo pipefail
docker exec smartadmin-api sh -c 'unzip -l /app/app.jar 2>/dev/null | grep -i SchemaService || echo NO_SchemaService_IN_JAR'
# public check
curl -sS -o /dev/null -w "public_captcha=%{http_code}\n" http://127.0.0.1:8081/api/login/getCaptcha
docker inspect smartadmin-api --format 'Created={{.Created}} Status={{.State.Status}}'