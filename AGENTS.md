# AGENTS.md

给 AI 编码 agent（Codex / Cursor / Claude Code 等）看的项目规则。

人类文档在 `deploy/` 下，目前有两份内容重叠的手册：`秒杀商城-生产部署手册-2026-09-13.md`（较新、较全，含整机重装流程）和 `部署文档.md`。**两份待合并成一份**，暂时以较新的手册为准；有冲突以手册为准，改完顺手把结论同步过去。

## 项目

SmartAdmin 商城（Java 17 + Spring Boot 3 后端、Vue 3 后台、uni-app 商城 H5），Docker Compose 部署到腾讯云 `175.27.131.7`。对外 HTTP 仍是 8080；无域名时可用自签证书开 443（`deploy/enable-https-selfsigned.sh`），浏览器会提示不安全。

| 端 | 目录 | 产物 |
| --- | --- | --- |
| 后端 | `smart-admin-api-java17-springboot3` | 镜像 `smartadmin-api`，容器内 1024 |
| 后台 | `smart-admin-web-javascript` | `dist/` |
| 商城 H5 | `smart-app` | `dist/build/h5/`，路由前缀 `/app/` |

`smart-admin-api-java8-springboot2`、`smart-admin-web-typescript` 是上游参考代码，**不要动、不要用来构建**。

## 硬规则（违反会出事故，请务必遵守）

1. **禁止 `docker compose down -v`**。会删掉 `mysql-data` / `redis-data` / `upload-data` 三个卷 = 清空生产库和上传文件，不可恢复。
2. **不要碰 `deploy/.env`**（生产库密码）。不要读它的值、不要改它、不要写进任何输出或提交。
3. **不要对生产服务器执行任何命令**。`~/.ssh/id_ed25519` 能直连 `ubuntu@175.27.131.7`，但部署动作由人按 `deploy/部署文档.md` 手动执行。
4. **`sa-base/src/main/resources/prod/sa-base.yaml` 里的 `url-prefix` 必须带 `:8080`**（`http://175.27.131.7:8080/upload/`）。少写端口，页面上所有图片 404，且该值打进 jar 必须重建后端才生效。
5. 改完后端要**同时**重启 nginx：`docker restart smartadmin-api && docker restart smartadmin-nginx`。只重启后端会全站 502（nginx 缓存了旧 IP）。
6. **不要执行** `sql/mysql/sql-update-log/` 下带版本号的脚本，它们已打进主脚本。也不要执行 `sql/mysql/restore-mall.sql`（内容已被 `restore-project-modules.sql` 覆盖）。

## 文件与编码约定

- 部署相关文件（`*.sh`、`Dockerfile`、`docker-compose*.yml`、`*.conf`）**必须 LF 换行、不能有 UTF-8 BOM**。Windows 工具默认写 CRLF/BOM，会导致 `set: pipefail: invalid option name` 或 nginx `unknown directive`。有 `.gitattributes` 兜底，但新建文件后请自查：
  `tr -dc '\r' < 文件 | wc -c` 必须为 0。
- 改完这些文件后，本地和服务器要逐字节一致（`md5sum` 对一遍），别靠肉眼。

## 常用命令

```bash
# 构建前端（服务器上执行）
docker run --rm -v /data/smartadmin/smart-admin-web-javascript:/app -w /app node:20-alpine \
  sh -c "npm config set registry https://registry.npmmirror.com && npm install && npm run build:prod"
docker run --rm -v /data/smartadmin/smart-app:/app -w /app node:20-alpine \
  sh -c "npm config set registry https://registry.npmmirror.com && npm install && npm run build:h5"

# 只重建后端
cd /data/smartadmin/deploy && docker compose -p smartadmin up -d --build sa-admin && docker restart smartadmin-nginx
```

## 验收基准（改完必须对得上）

- 库里 **65 张表**（官方 44 + 本项目 21）、**t_menu 179 行**（139 + 40）
- `curl http://127.0.0.1:8080/api/login/getCaptcha` 返回 JSON
- `/` 与 `/app/` 均 200
- 字段级：21 张模块表的列必须与 `sql/mysql/restore-project-modules.sql` 完全一致（表数对不代表字段对，本项目踩过漏列的坑）

## 数据库

- 官方系统表在 `sql/mysql/smart_admin_v3.sql`（44 张）
- 本项目扩展（商城/媒体/客服/支付）在 `sql/mysql/restore-project-modules.sql`（21 张 + 40 菜单 + 超管授权），**幂等**可重跑
- 那个文件同时会 DROP 并重建官方库，只有确认可以清库时才用；日常只导入 `restore-project-modules.sql` 即可
- **改库密码必须两步**：先 `ALTER USER 'root'@'%'` 和 `'localhost'`，再改 `deploy/.env`。只改 `.env` 库里的密码不会变，后端直接连不上库

## 给 agent 的协作建议

- 第一次接触本项目：**先只读**。读完 `deploy/部署文档.md` 和本文，复述项目用途、启动方式、核心目录、验收命令，确认无误再动手。
- 改动前先说明你要改哪些文件、为什么；改动后给出 `git diff` 和验证结果。
- 涉及支付、分销佣金、权限的改动，标记出来让人复核。
