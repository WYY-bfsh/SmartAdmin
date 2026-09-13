-- =============================================================================
-- 本项目 Web / H5 自建模块一次性恢复（在 smart_admin_v3.sql 之后执行）
-- 可重复执行。
--
-- 说明：Web 媒体中心几十个页面共用 t_media_*（目录类数据在 t_media_catalog.payload），
-- H5 秒杀页面共用 t_mall_*，不是一面一表。
-- Java 实体里出现的表已全部覆盖；官方系统表在 smart_admin_v3.sql。
-- =============================================================================

USE `smart_admin_v3`;
SET NAMES utf8mb4;

-- ---------- 秒杀商城（Web 后台 + H5）----------
CREATE TABLE IF NOT EXISTS `t_mall_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `password` varchar(64) NOT NULL,
  `invite_code` varchar(16) NOT NULL,
  `parent_member_id` bigint DEFAULT NULL,
  `commission_level` int DEFAULT NULL COMMENT '二级分销等级',
  `avatar` varchar(512) DEFAULT NULL,
  `wechat_pay_qr` varchar(512) DEFAULT NULL COMMENT '微信支付码',
  `wechat_receive_qr` varchar(512) DEFAULT NULL COMMENT '微信收款码',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_invite` (`invite_code`)
) COMMENT='秒杀商城会员';

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
) COMMENT='收货地址';

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
  `commission_rate_l2` decimal(6,4) DEFAULT NULL COMMENT '二级佣金比例',
  `enabled_flag` tinyint(1) NOT NULL DEFAULT 1,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`)
) COMMENT='秒杀活动';

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
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member` (`member_id`),
  KEY `idx_status` (`order_status`)
) COMMENT='秒杀订单';

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
) COMMENT='物流轨迹';

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
) COMMENT='一级分销佣金';

SET @exist := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_mall_commission' AND COLUMN_NAME = 'commission_level'
);
SET @sql := IF(@exist = 0,
  'ALTER TABLE `t_mall_commission` ADD COLUMN `commission_level` int DEFAULT NULL COMMENT ''1一级 2二级''',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `t_mall_setting` (
  `setting_id` bigint NOT NULL,
  `merchant_wechat_qr` varchar(512) DEFAULT NULL COMMENT '商家微信收款码',
  `merchant_alipay_qr` varchar(512) DEFAULT NULL COMMENT '商家支付宝收款码',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_id`)
) COMMENT='商城配置';

-- ---------- 媒体中心（AI漫剪 / 音乐 / 影月，多页面共用）----------
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
) COMMENT='AI漫剪作品';

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
) COMMENT='AI漫剪素材';

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
) COMMENT='AI漫剪成片任务';

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
) COMMENT='音乐歌曲';

CREATE TABLE IF NOT EXISTS `t_media_music_playlist` (
  `playlist_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `play_count` varchar(32) DEFAULT NULL,
  `song_count` int DEFAULT 0,
  `cover_url` varchar(512) DEFAULT NULL,
  `description` varchar(512) DEFAULT NULL,
  `song_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`playlist_id`)
) COMMENT='音乐歌单';

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
  `source` varchar(32) DEFAULT NULL,
  `source_id` varchar(64) DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`video_id`)
) COMMENT='影月影片';

CREATE TABLE IF NOT EXISTS `t_media_video_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `episode_no` int DEFAULT 1,
  `progress` int DEFAULT 0,
  `user_id` bigint DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`history_id`)
) COMMENT='影月观看历史';

CREATE TABLE IF NOT EXISTS `t_media_catalog` (
  `catalog_id` bigint NOT NULL AUTO_INCREMENT,
  `kind` varchar(32) NOT NULL,
  `payload` text NOT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`catalog_id`),
  KEY `idx_media_catalog_kind` (`kind`)
) COMMENT='媒体目录（模板/脚本/艺人/专辑/电台/海报 JSON）';

CREATE TABLE IF NOT EXISTS `t_media_user_action` (
  `action_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `action_type` varchar(32) NOT NULL,
  `biz_id` bigint NOT NULL,
  `extra` varchar(512) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`action_id`),
  UNIQUE KEY `uk_media_user_action` (`user_id`, `action_type`, `biz_id`)
) COMMENT='媒体用户行为';

-- ---------- 客服 ----------
CREATE TABLE IF NOT EXISTS `t_cs_ticket` (
  `ticket_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `ticket_no` varchar(32) NOT NULL COMMENT '工单编号',
  `title` varchar(200) NOT NULL COMMENT '工单标题',
  `ticket_type` int NOT NULL DEFAULT 1,
  `priority` int NOT NULL DEFAULT 2,
  `status` int NOT NULL DEFAULT 10,
  `content` text NOT NULL,
  `contact_name` varchar(50) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `create_user_id` bigint DEFAULT NULL,
  `handler_user_id` bigint DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  `close_time` datetime DEFAULT NULL,
  `reply_count` int NOT NULL DEFAULT 0,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  KEY `idx_status` (`status`)
) COMMENT='客服工单';

CREATE TABLE IF NOT EXISTS `t_cs_ticket_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `ticket_id` bigint NOT NULL,
  `message_type` int NOT NULL DEFAULT 1,
  `content` text NOT NULL,
  `create_user_id` bigint DEFAULT NULL,
  `create_name` varchar(50) DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `idx_ticket_id` (`ticket_id`)
) COMMENT='工单回复';

CREATE TABLE IF NOT EXISTS `t_cs_knowledge` (
  `knowledge_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `category` int NOT NULL DEFAULT 1,
  `content` longtext,
  `sort` int NOT NULL DEFAULT 0,
  `view_count` int NOT NULL DEFAULT 0,
  `create_user_id` bigint DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`knowledge_id`)
) COMMENT='客服知识库';

CREATE TABLE IF NOT EXISTS `t_cs_qa` (
  `qa_id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(500) NOT NULL,
  `answer` text,
  `status` int NOT NULL DEFAULT 10,
  `asker_name` varchar(50) DEFAULT NULL,
  `asker_phone` varchar(20) DEFAULT NULL,
  `asker_email` varchar(100) DEFAULT NULL,
  `create_user_id` bigint DEFAULT NULL,
  `answer_user_id` bigint DEFAULT NULL,
  `answer_time` datetime DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`qa_id`)
) COMMENT='客服问答';

-- ---------- 微信支付 ----------
CREATE TABLE IF NOT EXISTS `t_pay_order` (
  `pay_order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `description` varchar(127) NOT NULL,
  `amount` int NOT NULL,
  `trade_type` int NOT NULL DEFAULT 1,
  `pay_status` int NOT NULL DEFAULT 10,
  `code_url` varchar(512) DEFAULT NULL,
  `transaction_id` varchar(64) DEFAULT NULL,
  `openid` varchar(128) DEFAULT NULL,
  `payer_total` int DEFAULT NULL,
  `success_time` datetime DEFAULT NULL,
  `refund_no` varchar(64) DEFAULT NULL,
  `refund_id` varchar(64) DEFAULT NULL,
  `refund_amount` int NOT NULL DEFAULT 0,
  `refund_time` datetime DEFAULT NULL,
  `close_time` datetime DEFAULT NULL,
  `notify_content` text,
  `remark` varchar(500) DEFAULT NULL,
  `create_user_id` bigint DEFAULT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`pay_order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_pay_status` (`pay_status`)
) COMMENT='微信支付订单';

-- =============================================================================
-- 菜单（避开官方已占用的 menu_id=300 消息管理）
-- 支付 310+ / 媒体 400+ / 客服 500+ / 商城 600+
-- =============================================================================

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 310, '微信支付', 1, 0, 4, '/pay', NULL, NULL, NULL, NULL, 'WechatOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 310);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 311, '支付订单', 2, 310, 1, '/pay/order', '/business/pay/pay-order-list.vue', 'AccountBookOutlined', 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 311);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 312, '商户配置', 2, 310, 2, '/pay/config', '/business/pay/wechat-pay-config.vue', 'SettingOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 312);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 400, '媒体中心', 1, 0, 3, '/media', NULL, 'AppstoreOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 400);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 410, 'AI漫剪', 1, 400, 1, '/media/ai-clip', 'ScissorOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 410);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 411, '工作台', 2, 410, 1, '/media/ai-clip/workspace', '/business/media/ai-clip/workspace.vue', 'DashboardOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 411);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 412, '我的作品', 2, 410, 2, '/media/ai-clip/project', '/business/media/ai-clip/project-list.vue', 'FolderOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 412);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 414, '素材库', 2, 410, 4, '/media/ai-clip/material', '/business/media/ai-clip/material-list.vue', 'PictureOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 414);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 415, '模板中心', 2, 410, 5, '/media/ai-clip/template', '/business/media/ai-clip/template-list.vue', 'AppstoreOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 415);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 416, '脚本工坊', 2, 410, 7, '/media/ai-clip/script', '/business/media/ai-clip/script-list.vue', 'EditOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 416);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 417, '成片任务', 2, 410, 8, '/media/ai-clip/task', '/business/media/ai-clip/task-list.vue', 'ThunderboltOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 417);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 419, '导出发布', 2, 410, 9, '/media/ai-clip/export', '/business/media/ai-clip/export-list.vue', 'CloudUploadOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 419);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 420, '音乐播放器', 1, 400, 2, '/media/music', 'CustomerServiceOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 420);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 421, '发现音乐', 2, 420, 1, '/media/music/discover', '/business/media/music/discover.vue', 'FireOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 421);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 422, '歌单', 2, 420, 2, '/media/music/playlist', '/business/media/music/playlist-list.vue', 'UnorderedListOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 422);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 423, '歌手', 2, 420, 4, '/media/music/artist', '/business/media/music/artist-list.vue', 'UserOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 423);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 424, '专辑', 2, 420, 6, '/media/music/album', '/business/media/music/album-list.vue', 'AppstoreOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 424);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 425, '排行榜', 2, 420, 8, '/media/music/rank', '/business/media/music/rank.vue', 'TrophyOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 425);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 426, '播客电台', 2, 420, 9, '/media/music/radio', '/business/media/music/radio.vue', 'SoundOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 426);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 427, '我喜欢', 2, 420, 10, '/media/music/favorite', '/business/media/music/favorite.vue', 'HeartOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 427);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 428, '最近播放', 2, 420, 11, '/media/music/recent', '/business/media/music/recent.vue', 'HistoryOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 428);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 429, '搜索', 2, 420, 12, '/media/music/search', '/business/media/music/search.vue', 'SearchOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 429);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 430, '影月播放器', 1, 400, 3, '/media/yingyue', 'PlayCircleOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 430);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 431, '影月首页', 2, 430, 1, '/media/yingyue/home', '/business/media/yingyue/home.vue', 'HomeOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 431);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 432, '片库', 2, 430, 2, '/media/yingyue/library', '/business/media/yingyue/library.vue', 'AppstoreOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 432);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 433, '频道', 2, 430, 3, '/media/yingyue/channel', '/business/media/yingyue/channel.vue', 'VideoCameraOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 433);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 434, '影月排行', 2, 430, 4, '/media/yingyue/rank', '/business/media/yingyue/rank.vue', 'TrophyOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 434);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 435, '我的片单', 2, 430, 5, '/media/yingyue/favorite', '/business/media/yingyue/favorite.vue', 'StarOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 435);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 436, '观看历史', 2, 430, 6, '/media/yingyue/history', '/business/media/yingyue/history.vue', 'HistoryOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 436);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 437, '影月搜索', 2, 430, 7, '/media/yingyue/search', '/business/media/yingyue/search.vue', 'SearchOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 437);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 500, '客服中心', 1, 0, 5, '/customer', NULL, 'CustomerServiceOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 500);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 501, '工单管理', 2, 500, 1, '/customer/ticket', '/business/customer/ticket/ticket-list.vue', 'FileProtectOutlined', 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 501);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 502, '知识库', 2, 500, 2, '/customer/knowledge', '/business/customer/knowledge/knowledge-list.vue', 'BookOutlined', 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 502);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 503, '信息查询', 2, 500, 3, '/customer/info-query', '/business/customer/info-query/info-query.vue', 'SearchOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 503);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 512, '问答管理', 2, 500, 4, '/customer/qa', '/business/customer/qa/qa-list.vue', 'QuestionCircleOutlined', 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 512);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 600, '秒杀商城', 1, 0, 3, '/mall-admin', 'ShoppingOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 600);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 601, '秒杀活动', 2, 600, 1, '/mall-admin/activity', '/business/mall/admin/activity-list.vue', 'ThunderboltOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 601);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 602, '商城订单', 2, 600, 2, '/mall-admin/order', '/business/mall/admin/order-list.vue', 'AccountBookOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 602);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 603, '会员', 2, 600, 3, '/mall-admin/member', '/business/mall/admin/member-list.vue', 'TeamOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 603);
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 604, '分销佣金', 2, 600, 4, '/mall-admin/commission', '/business/mall/admin/commission-list.vue', 'PayCircleOutlined', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 604);

-- 超管角色（role_id=1）授权全部自建菜单
INSERT INTO `t_role_menu` (`role_id`, `menu_id`, `create_time`, `update_time`)
SELECT 1, m.menu_id, NOW(), NOW()
FROM `t_menu` m
WHERE m.menu_id IN (
  310, 311, 312,
  400, 410, 411, 412, 414, 415, 416, 417, 419,
  420, 421, 422, 423, 424, 425, 426, 427, 428, 429,
  430, 431, 432, 433, 434, 435, 436, 437,
  500, 501, 502, 503, 512,
  600, 601, 602, 603, 604
)
AND NOT EXISTS (
  SELECT 1 FROM `t_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);
