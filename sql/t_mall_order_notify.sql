-- 订单通知表（亦可执行 sql/mysql/sql-update-log/2026-09-16-formal-business-schema.sql）
USE `smart_admin_v3`;
CREATE TABLE IF NOT EXISTS `t_mall_order_notify` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL,
  `member_id` bigint DEFAULT NULL,
  `channel` varchar(16) NOT NULL DEFAULT 'SMS',
  `status` varchar(16) NOT NULL COMMENT 'SUCCESS/FAILED/PENDING',
  `event_id` varchar(64) DEFAULT NULL,
  `mobile` varchar(32) DEFAULT NULL,
  `content` varchar(512) DEFAULT NULL,
  `provider_msg_id` varchar(128) DEFAULT NULL,
  `error_msg` varchar(512) DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no_channel` (`order_no`,`channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单通知记录';