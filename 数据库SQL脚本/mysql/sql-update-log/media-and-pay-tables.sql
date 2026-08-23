-- 媒体中心 + 微信支付 新增表
-- 请先选中库：smart_admin_v3
-- 可重复执行（表已存在会跳过，菜单已存在会跳过）

SET NAMES utf8mb4;

-- =========================================================
-- 1. AI漫剪
-- =========================================================
CREATE TABLE IF NOT EXISTS `t_media_ai_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '作品ID',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `scene` int DEFAULT NULL COMMENT '场景 1带货 2教程 3Vlog 4漫剪 5资讯',
  `status` int NOT NULL DEFAULT 10 COMMENT '10草稿 20成片中 30已成片 40失败',
  `duration` varchar(16) DEFAULT NULL COMMENT '时长',
  `ratio` varchar(16) DEFAULT NULL COMMENT '画幅 9:16/16:9',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '封面',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除 0否 1是',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI漫剪作品';

CREATE TABLE IF NOT EXISTS `t_media_ai_material` (
  `material_id` bigint NOT NULL AUTO_INCREMENT COMMENT '素材ID',
  `name` varchar(128) NOT NULL COMMENT '名称',
  `type` int NOT NULL DEFAULT 1 COMMENT '1视频 2图片 3音频 4字幕',
  `duration` varchar(16) DEFAULT NULL COMMENT '时长',
  `size` varchar(32) DEFAULT NULL COMMENT '大小',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '封面',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除 0否 1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI漫剪素材';

CREATE TABLE IF NOT EXISTS `t_media_ai_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `type` varchar(32) DEFAULT NULL COMMENT '成片方式',
  `progress` int NOT NULL DEFAULT 0 COMMENT '进度0-100',
  `status` int NOT NULL DEFAULT 10 COMMENT '10排队 20生成中 30完成 40失败',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除 0否 1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI漫剪成片任务';

-- =========================================================
-- 2. 音乐播放器
-- =========================================================
CREATE TABLE IF NOT EXISTS `t_media_music_song` (
  `song_id` bigint NOT NULL AUTO_INCREMENT COMMENT '歌曲ID',
  `name` varchar(128) NOT NULL COMMENT '歌名',
  `artist` varchar(128) DEFAULT NULL COMMENT '歌手',
  `album` varchar(128) DEFAULT NULL COMMENT '专辑',
  `duration` varchar(16) DEFAULT NULL COMMENT '时长',
  `audio_url` varchar(512) DEFAULT NULL COMMENT '音频地址',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '封面',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除 0否 1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐歌曲';

CREATE TABLE IF NOT EXISTS `t_media_music_playlist` (
  `playlist_id` bigint NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name` varchar(128) NOT NULL COMMENT '歌单名',
  `play_count` varchar(32) DEFAULT NULL COMMENT '播放量',
  `song_count` int DEFAULT 0 COMMENT '歌曲数',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '封面',
  `description` varchar(512) DEFAULT NULL COMMENT '简介',
  PRIMARY KEY (`playlist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐歌单';

-- =========================================================
-- 3. 影月播放器
-- =========================================================
CREATE TABLE IF NOT EXISTS `t_media_video` (
  `video_id` bigint NOT NULL AUTO_INCREMENT COMMENT '影片ID',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `category` int DEFAULT NULL COMMENT '1电影 2电视剧 3综艺 4动漫 5纪录片',
  `area` varchar(16) DEFAULT NULL COMMENT 'cn/us/kr/jp/other',
  `year` int DEFAULT NULL COMMENT '年份',
  `play_url` varchar(512) DEFAULT NULL COMMENT '播放地址',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '封面',
  `intro` varchar(1000) DEFAULT NULL COMMENT '简介',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除 0否 1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影月影片';

CREATE TABLE IF NOT EXISTS `t_media_video_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `video_id` bigint NOT NULL COMMENT '影片ID',
  `episode_no` int DEFAULT 1 COMMENT '集数',
  `progress` int DEFAULT 0 COMMENT '进度0-100',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影月观看历史';

-- =========================================================
-- 4. 微信支付（上一批功能，没建过就一起建）
-- =========================================================
CREATE TABLE IF NOT EXISTS `t_pay_order` (
  `pay_order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '商户订单号',
  `description` varchar(127) NOT NULL COMMENT '商品描述',
  `amount` int NOT NULL COMMENT '订单金额，单位分',
  `trade_type` int NOT NULL DEFAULT 1 COMMENT '支付方式 1扫码支付',
  `pay_status` int NOT NULL DEFAULT 10 COMMENT '10待支付 20成功 30关闭 40退款中 50已退款',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信支付订单';
