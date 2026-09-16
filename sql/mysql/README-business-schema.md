# 业务库 SQL（正式开发约定）

## 原则
- 表结构只维护在本目录 SQL 中，应用代码不再自动 CREATE/ALTER。
- 已删除 `MallSchemaService` / `PaySchemaService` / `MediaSchemaService`。

## 文件
| 文件 | 用途 |
|------|------|
| `sql-update-log/2026-09-16-formal-business-schema.sql` | 可重复执行的正式增量（建表+缺列） |
| `restore-mall.sql` | 商城表全量恢复 |
| `sql-update-log/mall-order-notify.sql` | 订单通知表 |
| `online-business-schema-dump.sql` | 从线上库导出的结构快照（无数据） |
| `online-business-table-rows.txt` | 导出时线上行数参考 |

## 上线执行
```bash
mysql -u... -p smart_admin_v3 < sql/mysql/sql-update-log/2026-09-16-formal-business-schema.sql
```