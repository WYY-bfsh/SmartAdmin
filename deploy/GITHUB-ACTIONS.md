# GitHub Actions 生产部署（SmartAdmin API）

## 已就绪

- 工作流：`.github/workflows/deploy-prod.yml`
  - 触发：`master`/`main` 上 API/compose/workflow 变更，或手动 **Run workflow**
  - 动作：打 API 源码包 → SCP → 服务器 Docker 构建 `sa-admin` → 健康检查
  - **不改** 端口 / `deploy/.env`；nginx 用当前宿主端口（现为 8081）
- 服务器专用密钥：`~/.ssh/gha_deploy`（已写入 `authorized_keys`）
- 本机私钥副本（勿提交 Git）：`deploy/secrets/gha_deploy`
- `.gitignore` 已忽略 `deploy/secrets/`

## 你需要完成的 4 步

### 1. 创建 GitHub 私有仓库

浏览器打开 https://github.com/new  

- Repository name：建议 `smart-admin`
- 选 **Private**
- **不要**勾选 Add README / .gitignore / license（仓库保持空，方便 push）

创建后记下地址，例如：`https://github.com/<你的用户名>/smart-admin.git`

### 2. 添加 remote 并推送（本机项目目录）

当前分支是 `payTest`，工作流只监听 `master`/`main`。建议：

```powershell
cd D:\Personal\project\smartadmin
git remote add github https://github.com/<你的用户名>/smart-admin.git
# 先提交工作流与相关改动（按你自己习惯 commit）
git push -u github payTest
# 再把含工作流的提交推到 master（或把 master 指到要部署的提交）
git push github payTest:master
```

Gitee 的 `origin` 可保留不动。

### 3. 配置 GitHub Secrets

仓库 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Name | Value |
|------|--------|
| `DEPLOY_HOST` | `175.27.131.7` |
| `DEPLOY_USER` | `ubuntu` |
| `DEPLOY_SSH_KEY` | 打开本机文件 `D:\Personal\project\smartadmin\deploy\secrets\gha_deploy`，**整份私钥**粘贴（含 `BEGIN`/`END` 行） |

### 4. 跑一次部署

仓库 → **Actions** → **Deploy production API** → **Run workflow**（选 `master`）

成功后访问：http://175.27.131.7:8081/api/login/getCaptcha 应为 200。

之后：向 `master`/`main` 推送 API 相关改动会自动部署。

## 注意

- 私钥只放在 Secrets / 本机 `deploy/secrets/`，不要贴到聊天、不要 commit
- 首次 push 若 GitHub 要登录，用浏览器 Personal Access Token 或 Git Credential Manager