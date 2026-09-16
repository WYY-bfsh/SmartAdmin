-- 秒杀商城表（正式库侧脚本，可重复执行）
-- 先导入 smart_admin_v3.sql，再执行本文件
-- 增量字段见 sql-update-log/2026-09-16-formal-business-schema.sql

USE `smart_admin_v3`;

-- ========== 秒杀商城 ==========
CREATE TABLE IF NOT EXISTS `t_mall_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `password` varchar(64) NOT NULL,
  `invite_code` varchar(16) NOT NULL,
  `parent_member_id` bigint DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `wechat_pay_qr` varchar(512) DEFAULT NULL COMMENT '微信支付码',
  `wechat_receive_qr` varchar(512) DEFAULT NULL COMMENT '微信收款码',
  `wechat_openid` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `commission_level` int DEFAULT NULL COMMENT '分销等级',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_invite` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商城会员';

CREATE TABLE IF NOT EXISTS `t_mall_address` (
  `address_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `receiver_name` varchar(32) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province` varchar(32) DEFAULT NULL,
  `city` varchar(32) DEFAULT NULL,
  `district` varchar(32) DEFAULT NULL,
  `detail` varchar(255) NOT NULL,
  `default_flag` tinyint(1) NOT NULL DEFAULT 0,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  KEY `idx_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

CREATE TABLE IF NOT EXISTS `t_seckill_activity` (
  `activity_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(64) NOT NULL,
  `goods_name` varchar(128) NOT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `detail` varchar(2000) DEFAULT NULL,
  `origin_price` decimal(10,2) NOT NULL,
  `seckill_price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT 0,
  `sold_count` int NOT NULL DEFAULT 0,
  `per_limit` int NOT NULL DEFAULT 1,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `concurrent_limit` int DEFAULT NULL,
  `commission_rate` decimal(6,4) DEFAULT NULL,
  `commission_rate_l2` decimal(6,4) DEFAULT NULL COMMENT '二级分销比例',
  `enabled_flag` tinyint(1) NOT NULL DEFAULT 1,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动';

CREATE TABLE IF NOT EXISTS `t_mall_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `member_id` bigint NOT NULL,
  `activity_id` bigint NOT NULL,
  `goods_name` varchar(128) NOT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `qty` int NOT NULL DEFAULT 1,
  `price` decimal(10,2) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `pay_status` int NOT NULL DEFAULT 10,
  `order_status` int NOT NULL DEFAULT 10,
  `receiver_name` varchar(32) DEFAULT NULL,
  `receiver_phone` varchar(20) DEFAULT NULL,
  `receiver_address` varchar(255) DEFAULT NULL,
  `express_code` varchar(32) DEFAULT NULL,
  `express_name` varchar(32) DEFAULT NULL,
  `waybill_no` varchar(64) DEFAULT NULL,
  `ship_time` datetime DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `close_time` datetime DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `parent_member_id` bigint DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `pay_proof_url` varchar(512) DEFAULT NULL COMMENT '付款截图',
  `pay_note` varchar(255) DEFAULT NULL COMMENT '付款说明',
  `pay_channel` int DEFAULT NULL COMMENT '10线下 20微信',
  `wx_transaction_id` varchar(64) DEFAULT NULL COMMENT '微信支付单号',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member` (`member_id`),
  KEY `idx_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单';

CREATE TABLE IF NOT EXISTS `t_mall_express_trace` (
  `trace_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `ftime` varchar(32) NOT NULL,
  `context` varchar(255) NOT NULL,
  `status_text` varchar(32) DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`trace_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹';

CREATE TABLE IF NOT EXISTS `t_mall_commission` (
  `commission_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `from_member_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `order_no` varchar(32) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `rate` decimal(6,4) NOT NULL,
  `commission_level` int DEFAULT NULL COMMENT '1一级 2二级',
  `status` int NOT NULL DEFAULT 10,
  `settle_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`commission_id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一级分销佣金';

CREATE TABLE IF NOT EXISTS `t_mall_setting` (
  `setting_id` bigint NOT NULL,
  `merchant_wechat_qr` varchar(512) DEFAULT NULL COMMENT '商家微信收款码',
  `merchant_alipay_qr` varchar(512) DEFAULT NULL COMMENT '商家支付宝收款码',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城配置';

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
