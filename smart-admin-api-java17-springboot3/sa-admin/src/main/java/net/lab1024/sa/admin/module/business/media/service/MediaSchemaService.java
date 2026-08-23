package net.lab1024.sa.admin.module.business.media.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体中心缺表/缺列时自动补齐，避免必须手工跑 SQL。
 */
@Slf4j
@Service
public class MediaSchemaService {

    @Resource
    private DataSource dataSource;

    public void ensureTables() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            for (String sql : createTableSql()) {
                st.execute(sql);
            }
            ensureColumn(conn, st, "t_media_ai_material", "file_key", "varchar(255) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_ai_material", "file_url", "varchar(512) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_ai_task", "result_url", "varchar(512) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_music_song", "lyric", "text");
            ensureColumn(conn, st, "t_media_music_playlist", "song_ids", "varchar(255) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_video", "score", "decimal(4,1) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_video", "episode_count", "int DEFAULT 1");
            ensureColumn(conn, st, "t_media_video", "update_info", "varchar(64) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_video", "source", "varchar(32) DEFAULT NULL");
            ensureColumn(conn, st, "t_media_video", "source_id", "varchar(64) DEFAULT NULL");
            log.info("媒体中心数据表已就绪");
        } catch (Exception e) {
            throw new IllegalStateException("自动创建媒体中心表失败：" + e.getMessage(), e);
        }
    }

    private void ensureColumn(Connection conn, Statement st, String table, String column, String definition) throws Exception {
        try (ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(), null, table, column)) {
            if (rs.next()) {
                return;
            }
        }
        st.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
    }

    private List<String> createTableSql() {
        List<String> list = new ArrayList<>();
        list.add("""
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
                ) COMMENT='AI漫剪作品'
                """);
        list.add("""
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
                ) COMMENT='AI漫剪素材'
                """);
        list.add("""
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
                ) COMMENT='AI漫剪成片任务'
                """);
        list.add("""
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
                ) COMMENT='音乐歌曲'
                """);
        list.add("""
                CREATE TABLE IF NOT EXISTS `t_media_music_playlist` (
                  `playlist_id` bigint NOT NULL AUTO_INCREMENT,
                  `name` varchar(128) NOT NULL,
                  `play_count` varchar(32) DEFAULT NULL,
                  `song_count` int DEFAULT 0,
                  `cover_url` varchar(512) DEFAULT NULL,
                  `description` varchar(512) DEFAULT NULL,
                  `song_ids` varchar(255) DEFAULT NULL,
                  PRIMARY KEY (`playlist_id`)
                ) COMMENT='音乐歌单'
                """);
        list.add("""
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
                ) COMMENT='影月影片'
                """);
        list.add("""
                CREATE TABLE IF NOT EXISTS `t_media_video_history` (
                  `history_id` bigint NOT NULL AUTO_INCREMENT,
                  `video_id` bigint NOT NULL,
                  `episode_no` int DEFAULT 1,
                  `progress` int DEFAULT 0,
                  `user_id` bigint DEFAULT NULL,
                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`history_id`)
                ) COMMENT='影月观看历史'
                """);
        list.add("""
                CREATE TABLE IF NOT EXISTS `t_media_catalog` (
                  `catalog_id` bigint NOT NULL AUTO_INCREMENT,
                  `kind` varchar(32) NOT NULL,
                  `payload` text NOT NULL,
                  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (`catalog_id`),
                  KEY `idx_media_catalog_kind` (`kind`)
                ) COMMENT='媒体中心目录'
                """);
        list.add("""
                CREATE TABLE IF NOT EXISTS `t_media_user_action` (
                  `action_id` bigint NOT NULL AUTO_INCREMENT,
                  `user_id` bigint NOT NULL,
                  `action_type` varchar(32) NOT NULL,
                  `biz_id` bigint NOT NULL,
                  `extra` varchar(512) DEFAULT NULL,
                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`action_id`),
                  UNIQUE KEY `uk_media_user_action` (`user_id`, `action_type`, `biz_id`)
                ) COMMENT='媒体中心用户行为'
                """);
        return list;
    }
}
