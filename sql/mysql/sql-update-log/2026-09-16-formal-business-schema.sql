-- SmartAdmin 业务表正式落库（商城 / 支付 / 媒体 / 订单通知）
-- 可重复执行：CREATE IF NOT EXISTS + 缺列才 ADD
-- 用法：mysql -u... -p smart_admin_v3 < 本文件

USE `smart_admin_v3`;

DROP PROCEDURE IF EXISTS `sa_add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `sa_add_column_if_missing`(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

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

-- ========== 支付 ==========
CREATE TABLE IF NOT EXISTS `t_pay_order` (
  `pay_order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '商户订单号',
  `description` varchar(127) NOT NULL COMMENT '商品描述',
  `amount` int NOT NULL COMMENT '订单金额，单位分',
  `trade_type` int NOT NULL DEFAULT 1 COMMENT '支付方式 1扫码支付',
  `pay_status` int NOT NULL DEFAULT 10 COMMENT '支付状态',
  `code_url` varchar(512) DEFAULT NULL COMMENT 'Native二维码内容',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '微信支付订单号',
  `openid` varchar(128) DEFAULT NULL COMMENT '用户标识',
  `payer_total` int DEFAULT NULL COMMENT '用户实付金额，单位分',
  `success_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `refund_no` varchar(64) DEFAULT NULL COMMENT '商户退款单号',
  `refund_id` varchar(64) DEFAULT NULL COMMENT '微信退款单号',
  `refund_amount` int NOT NULL DEFAULT 0 COMMENT '已退款金额，单位分',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `notify_content` text COMMENT '回调原文',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`pay_order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_pay_status` (`pay_status`)
  ) COMMENT='微信支付订单' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_pay_recon_batch` (
  `batch_id` bigint NOT NULL AUTO_INCREMENT COMMENT '对账批次ID',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `pay_channel` int NOT NULL COMMENT '支付渠道 1微信 2支付宝',
  `source_type` int NOT NULL COMMENT '来源 1拉取 2上传 3演示',
  `batch_status` int NOT NULL DEFAULT 10 COMMENT '批次状态 10处理中 20完成 30失败',
  `local_count` int NOT NULL DEFAULT 0 COMMENT '本地笔数',
  `channel_count` int NOT NULL DEFAULT 0 COMMENT '渠道笔数',
  `matched_count` int NOT NULL DEFAULT 0 COMMENT '完全匹配',
  `amount_diff_count` int NOT NULL DEFAULT 0 COMMENT '金额不符',
  `status_diff_count` int NOT NULL DEFAULT 0 COMMENT '状态不符',
  `local_only_count` int NOT NULL DEFAULT 0 COMMENT '仅本地有',
  `channel_only_count` int NOT NULL DEFAULT 0 COMMENT '仅渠道有',
  `local_amount` int NOT NULL DEFAULT 0 COMMENT '本地金额分',
  `channel_amount` int NOT NULL DEFAULT 0 COMMENT '渠道金额分',
  `file_name` varchar(255) DEFAULT NULL COMMENT '账单文件名',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`batch_id`),
  KEY `idx_bill_date_channel` (`bill_date`, `pay_channel`)
  ) COMMENT='支付对账批次' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_pay_recon_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `match_status` int NOT NULL COMMENT '匹配结果',
  `biz_type` int NOT NULL DEFAULT 1 COMMENT '1交易 2退款',
  `pay_order_id` bigint DEFAULT NULL COMMENT '本地支付单',
  `order_no` varchar(64) DEFAULT NULL COMMENT '商户订单号',
  `local_amount` int DEFAULT NULL COMMENT '本地金额分',
  `local_status` int DEFAULT NULL COMMENT '本地支付状态',
  `channel_trade_no` varchar(64) DEFAULT NULL COMMENT '渠道交易号',
  `channel_order_no` varchar(64) DEFAULT NULL COMMENT '渠道商户单号',
  `channel_amount` int DEFAULT NULL COMMENT '渠道金额分',
  `channel_status` varchar(64) DEFAULT NULL COMMENT '渠道状态原文',
  `channel_time` varchar(64) DEFAULT NULL COMMENT '渠道完成时间',
  `diff_amount` int DEFAULT NULL COMMENT '差额分',
  `handled_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已人工核销',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_match_status` (`match_status`),
  KEY `idx_order_no` (`order_no`)
  ) COMMENT='支付对账明细' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;


-- ========== 媒体 ==========
CREATE TABLE IF NOT EXISTS `t_media_ai_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL,
  `scene` int DEFAULT NULL,
  `status` int NOT NULL DEFAULT 10,
  `duration` varchar(16) DEFAULT NULL,
  `ratio` varchar(16) DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`project_id`)
  ) COMMENT='AI漫剪作品' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_ai_material` (
  `material_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `type` int NOT NULL DEFAULT 1,
  `duration` varchar(16) DEFAULT NULL,
  `size` varchar(32) DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `file_key` varchar(255) DEFAULT NULL,
  `file_url` varchar(512) DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`material_id`)
  ) COMMENT='AI漫剪素材' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_ai_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL,
  `type` varchar(32) DEFAULT NULL,
  `progress` int NOT NULL DEFAULT 0,
  `status` int NOT NULL DEFAULT 10,
  `result_url` varchar(512) DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`)
  ) COMMENT='AI漫剪成片任务' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_music_song` (
  `song_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `artist` varchar(128) DEFAULT NULL,
  `album` varchar(128) DEFAULT NULL,
  `duration` varchar(16) DEFAULT NULL,
  `audio_url` varchar(512) DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `lyric` text,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`song_id`)
  ) COMMENT='音乐歌曲' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_music_playlist` (
  `playlist_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `play_count` varchar(32) DEFAULT NULL,
  `song_count` int DEFAULT 0,
  `cover_url` varchar(512) DEFAULT NULL,
  `description` varchar(512) DEFAULT NULL,
  `song_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`playlist_id`)
  ) COMMENT='音乐歌单' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_video` (
  `video_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL,
  `category` int DEFAULT NULL,
  `area` varchar(16) DEFAULT NULL,
  `year` int DEFAULT NULL,
  `score` decimal(4,1) DEFAULT NULL,
  `episode_count` int DEFAULT 1,
  `update_info` varchar(64) DEFAULT NULL,
  `play_url` varchar(512) DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `intro` varchar(1000) DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`video_id`)
  ) COMMENT='影月影片' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_video_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `episode_no` int DEFAULT 1,
  `progress` int DEFAULT 0,
  `user_id` bigint DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`history_id`)
  ) COMMENT='影月观看历史' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_catalog` (
  `catalog_id` bigint NOT NULL AUTO_INCREMENT,
  `kind` varchar(32) NOT NULL,
  `payload` text NOT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`catalog_id`),
  KEY `idx_media_catalog_kind` (`kind`)
  ) COMMENT='媒体中心目录' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;

CREATE TABLE IF NOT EXISTS `t_media_user_action` (
  `action_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `action_type` varchar(32) NOT NULL,
  `biz_id` bigint NOT NULL,
  `extra` varchar(512) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`action_id`),
  UNIQUE KEY `uk_media_user_action` (`user_id`, `action_type`, `biz_id`)
  ) COMMENT='媒体中心用户行为' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
;


-- 存量库补列（已存在则跳过）
CALL sa_add_column_if_missing('t_mall_member', 'wechat_pay_qr', 'varchar(512) DEFAULT NULL COMMENT ''微信支付码''');
CALL sa_add_column_if_missing('t_mall_member', 'wechat_receive_qr', 'varchar(512) DEFAULT NULL COMMENT ''微信收款码''');
CALL sa_add_column_if_missing('t_mall_member', 'wechat_openid', 'varchar(64) DEFAULT NULL COMMENT ''微信openid''');
CALL sa_add_column_if_missing('t_mall_member', 'commission_level', 'int DEFAULT NULL COMMENT ''分销等级''');
CALL sa_add_column_if_missing('t_mall_order', 'pay_proof_url', 'varchar(512) DEFAULT NULL COMMENT ''付款截图''');
CALL sa_add_column_if_missing('t_mall_order', 'pay_note', 'varchar(255) DEFAULT NULL COMMENT ''付款说明''');
CALL sa_add_column_if_missing('t_mall_order', 'pay_channel', 'int DEFAULT NULL COMMENT ''10线下 20微信''');
CALL sa_add_column_if_missing('t_mall_order', 'wx_transaction_id', 'varchar(64) DEFAULT NULL COMMENT ''微信支付单号''');
CALL sa_add_column_if_missing('t_seckill_activity', 'commission_rate_l2', 'decimal(6,4) DEFAULT NULL COMMENT ''二级分销比例''');
CALL sa_add_column_if_missing('t_mall_commission', 'commission_level', 'int DEFAULT NULL COMMENT ''1一级 2二级''');
CALL sa_add_column_if_missing('t_pay_order', 'mall_order_id', 'bigint DEFAULT NULL COMMENT ''秒杀订单ID''');
CALL sa_add_column_if_missing('t_pay_order', 'pay_channel', 'int NOT NULL DEFAULT 1 COMMENT ''支付渠道 1微信 2支付宝''');

DROP PROCEDURE IF EXISTS `sa_add_column_if_missing`;