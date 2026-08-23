-- 媒体中心商业化补丁（已执行过 media-center.sql 的库执行本文件）
-- 若某列已存在报 Duplicate column，忽略该句即可
-- 执行库：smart_admin_v3

ALTER TABLE `t_media_ai_material`
  ADD COLUMN `file_key` varchar(255) DEFAULT NULL AFTER `cover_url`,
  ADD COLUMN `file_url` varchar(512) DEFAULT NULL AFTER `file_key`;

ALTER TABLE `t_media_ai_task`
  ADD COLUMN `result_url` varchar(512) DEFAULT NULL AFTER `status`;

ALTER TABLE `t_media_music_song`
  ADD COLUMN `lyric` text AFTER `cover_url`;

ALTER TABLE `t_media_music_playlist`
  ADD COLUMN `song_ids` varchar(255) DEFAULT NULL AFTER `description`;

ALTER TABLE `t_media_video`
  ADD COLUMN `score` decimal(4,1) DEFAULT NULL AFTER `year`,
  ADD COLUMN `episode_count` int DEFAULT 1 AFTER `score`,
  ADD COLUMN `update_info` varchar(64) DEFAULT NULL AFTER `episode_count`;

CREATE TABLE IF NOT EXISTS `t_media_catalog` (
  `catalog_id` bigint NOT NULL AUTO_INCREMENT,
  `kind` varchar(32) NOT NULL,
  `payload` text NOT NULL,
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`catalog_id`),
  KEY `idx_media_catalog_kind` (`kind`)
) COMMENT='媒体中心目录（模板/脚本/艺人/专辑/电台/海报）';

ALTER TABLE `t_media_video`
  ADD COLUMN `source` varchar(32) DEFAULT NULL AFTER `intro`,
  ADD COLUMN `source_id` varchar(64) DEFAULT NULL AFTER `source`;

CREATE TABLE IF NOT EXISTS `t_media_user_action` (
  `action_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `action_type` varchar(32) NOT NULL,
  `biz_id` bigint NOT NULL,
  `extra` varchar(512) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`action_id`),
  UNIQUE KEY `uk_media_user_action` (`user_id`, `action_type`, `biz_id`)
) COMMENT='媒体中心用户行为（喜欢/片单/最近播放）';

-- 把国内无法访问的 Google 样片地址换成西瓜播放器公开演示 MP4
UPDATE `t_media_video`
SET `play_url` = 'https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4'
WHERE `play_url` IS NULL
   OR `play_url` = ''
   OR `play_url` LIKE '%googleapis%'
   OR `play_url` LIKE '%gtv-videos-bucket%';
