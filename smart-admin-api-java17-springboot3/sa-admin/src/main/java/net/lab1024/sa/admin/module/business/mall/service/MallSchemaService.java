package net.lab1024.sa.admin.module.business.mall.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Service
public class MallSchemaService {

    @Resource
    private DataSource dataSource;

    private volatile boolean ready = false;

    public void ensureTables() {
        if (ready) {
            return;
        }
        synchronized (this) {
            if (ready) {
                return;
            }
            try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
                st.execute("""
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
                          `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`member_id`),
                          UNIQUE KEY `uk_phone` (`phone`),
                          UNIQUE KEY `uk_invite` (`invite_code`)
                        ) COMMENT='秒杀商城会员'
                        """);
                st.execute("""
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
                        ) COMMENT='收货地址'
                        """);
                st.execute("""
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
                          `enabled_flag` tinyint(1) NOT NULL DEFAULT 1,
                          `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`activity_id`)
                        ) COMMENT='秒杀活动'
                        """);
                st.execute("""
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
                          `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`order_id`),
                          UNIQUE KEY `uk_order_no` (`order_no`),
                          KEY `idx_member` (`member_id`),
                          KEY `idx_status` (`order_status`)
                        ) COMMENT='秒杀订单'
                        """);
                st.execute("""
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
                        ) COMMENT='物流轨迹'
                        """);
                st.execute("""
                        CREATE TABLE IF NOT EXISTS `t_mall_commission` (
                          `commission_id` bigint NOT NULL AUTO_INCREMENT,
                          `member_id` bigint NOT NULL,
                          `from_member_id` bigint NOT NULL,
                          `order_id` bigint NOT NULL,
                          `order_no` varchar(32) NOT NULL,
                          `amount` decimal(10,2) NOT NULL,
                          `rate` decimal(6,4) NOT NULL,
                          `status` int NOT NULL DEFAULT 10,
                          `settle_time` datetime DEFAULT NULL,
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`commission_id`),
                          UNIQUE KEY `uk_order` (`order_id`),
                          KEY `idx_member` (`member_id`)
                        )                         COMMENT='一级分销佣金'
                        """);
                st.execute("""
                        CREATE TABLE IF NOT EXISTS `t_mall_setting` (
                          `setting_id` bigint NOT NULL,
                          `merchant_wechat_qr` varchar(512) DEFAULT NULL COMMENT '商家微信收款码',
                          `merchant_alipay_qr` varchar(512) DEFAULT NULL COMMENT '商家支付宝收款码',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`setting_id`)
                        ) COMMENT='商城配置'
                        """);
                addColumnIfMissing(conn, "t_mall_member", "wechat_pay_qr", "varchar(512) DEFAULT NULL COMMENT '微信支付码'");
                addColumnIfMissing(conn, "t_mall_member", "wechat_receive_qr", "varchar(512) DEFAULT NULL COMMENT '微信收款码'");
                addColumnIfMissing(conn, "t_mall_order", "pay_proof_url", "varchar(512) DEFAULT NULL COMMENT '付款截图'");
                addColumnIfMissing(conn, "t_mall_order", "pay_note", "varchar(255) DEFAULT NULL COMMENT '付款说明'");
                ready = true;
                log.info("秒杀商城数据表已就绪");
            } catch (Exception e) {
                throw new IllegalStateException("自动创建秒杀商城表失败：" + e.getMessage(), e);
            }
        }
    }

    private void addColumnIfMissing(Connection conn, String table, String column, String ddl) {
        try (Statement query = conn.createStatement();
             var rs = query.executeQuery("SHOW COLUMNS FROM `" + table + "` LIKE '" + column + "'")) {
            if (rs.next()) {
                return;
            }
        } catch (Exception e) {
            log.warn("检查表字段失败 {}.{}: {}", table, column, e.getMessage());
            return;
        }
        try (Statement alter = conn.createStatement()) {
            alter.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + ddl);
            log.info("已为 {}.{} 增加字段", table, column);
        } catch (Exception e) {
            log.warn("增加表字段失败 {}.{}: {}", table, column, e.getMessage());
        }
    }
}
