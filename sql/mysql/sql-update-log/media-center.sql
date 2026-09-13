-- 媒体中心：AI漫剪 / 音乐播放器 / 影月播放器
-- 执行库：smart_admin_v3

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
) COMMENT='媒体中心目录（模板/脚本/艺人/专辑/电台/海报）';

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

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 400, '媒体中心', 1, 0, 3, '/media', NULL, 'AppstoreOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 400);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 410, 'AI漫剪', 1, 400, 1, '/media/ai-clip', 'ScissorOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 410);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 411, '工作台', 2, 410, 1, '/media/ai-clip/workspace', '/business/media/ai-clip/workspace.vue', 'DashboardOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 411);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 412, '我的作品', 2, 410, 2, '/media/ai-clip/project', '/business/media/ai-clip/project-list.vue', 'FolderOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 412);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 414, '素材库', 2, 410, 4, '/media/ai-clip/material', '/business/media/ai-clip/material-list.vue', 'PictureOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 414);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 415, '模板中心', 2, 410, 5, '/media/ai-clip/template', '/business/media/ai-clip/template-list.vue', 'AppstoreOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 415);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 416, '脚本工坊', 2, 410, 7, '/media/ai-clip/script', '/business/media/ai-clip/script-list.vue', 'EditOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 416);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 417, '成片任务', 2, 410, 8, '/media/ai-clip/task', '/business/media/ai-clip/task-list.vue', 'ThunderboltOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 417);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 419, '导出发布', 2, 410, 9, '/media/ai-clip/export', '/business/media/ai-clip/export-list.vue', 'CloudUploadOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 419);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 420, '音乐播放器', 1, 400, 2, '/media/music', 'CustomerServiceOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 420);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 421, '发现音乐', 2, 420, 1, '/media/music/discover', '/business/media/music/discover.vue', 'FireOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 421);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 422, '歌单', 2, 420, 2, '/media/music/playlist', '/business/media/music/playlist-list.vue', 'UnorderedListOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 422);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 423, '歌手', 2, 420, 4, '/media/music/artist', '/business/media/music/artist-list.vue', 'UserOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 423);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 424, '专辑', 2, 420, 6, '/media/music/album', '/business/media/music/album-list.vue', 'AppstoreOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 424);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 425, '排行榜', 2, 420, 8, '/media/music/rank', '/business/media/music/rank.vue', 'TrophyOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 425);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 426, '播客电台', 2, 420, 9, '/media/music/radio', '/business/media/music/radio.vue', 'SoundOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 426);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 427, '我喜欢', 2, 420, 10, '/media/music/favorite', '/business/media/music/favorite.vue', 'HeartOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 427);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 428, '最近播放', 2, 420, 11, '/media/music/recent', '/business/media/music/recent.vue', 'HistoryOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 428);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 429, '搜索', 2, 420, 12, '/media/music/search', '/business/media/music/search.vue', 'SearchOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 429);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 430, '影月播放器', 1, 400, 3, '/media/yingyue', 'PlayCircleOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 430);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 431, '影月首页', 2, 430, 1, '/media/yingyue/home', '/business/media/yingyue/home.vue', 'HomeOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 431);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 432, '片库', 2, 430, 2, '/media/yingyue/library', '/business/media/yingyue/library.vue', 'AppstoreOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 432);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 433, '频道', 2, 430, 3, '/media/yingyue/channel', '/business/media/yingyue/channel.vue', 'VideoCameraOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 433);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 434, '排行榜', 2, 430, 4, '/media/yingyue/rank', '/business/media/yingyue/rank.vue', 'TrophyOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 434);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 435, '我的片单', 2, 430, 5, '/media/yingyue/favorite', '/business/media/yingyue/favorite.vue', 'StarOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 435);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 436, '观看历史', 2, 430, 6, '/media/yingyue/history', '/business/media/yingyue/history.vue', 'HistoryOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 436);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 437, '搜索', 2, 430, 7, '/media/yingyue/search', '/business/media/yingyue/search.vue', 'SearchOutlined', 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 437);
