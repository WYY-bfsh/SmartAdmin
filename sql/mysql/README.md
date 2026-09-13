### 数据库脚本
默认数据库为Mysql，若为其他数据库，请关注：[SmartAdmin其他数据库](https://smartadmin.vip/views/other/china-db/)

#### 第一次 / 库被清空后重建

1. 先执行 `smart_admin_v3.sql`（会 `DROP DATABASE` 再建库，系统表和默认数据都在这里）。
2. 再执行 **`restore-project-modules.sql`**（商城/媒体/客服/支付的全部表、菜单、超管权限）。  
   官方增量 v3.15～v3.30 已打进主脚本，不要再跑带版本号的文件。

服务器 Docker 一键恢复：`bash /data/smartadmin/deploy/恢复数据库.sh`

#### 更新
跟随 SmartAdmin 官方小版本升级时，再执行 `sql-update-log` 里带版本号的脚本（从小到大）。  
`media-center-online.sql`、`media-and-pay-tables.sql` 是给旧库打补丁的，全新导入不必执行。
