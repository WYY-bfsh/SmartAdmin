package net.lab1024.sa.admin.module.business.pay.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 支付订单表缺失时自动创建，避免必须手工执行 SQL。
 */
@Slf4j
@Service
public class PaySchemaService {

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
                        ) COMMENT='微信支付订单'
                        """);
                addColumnIfMissing(conn, "t_pay_order", "mall_order_id", "bigint DEFAULT NULL COMMENT '秒杀订单ID'");
                addColumnIfMissing(conn, "t_pay_order", "pay_channel",
                        "int NOT NULL DEFAULT 1 COMMENT '支付渠道 1微信 2支付宝'");
                addIndexIfMissing(conn, "t_pay_order", "idx_pay_channel", "(`pay_channel`)");
                st.execute("""
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
                        ) COMMENT='支付对账批次'
                        """);
                st.execute("""
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
                        ) COMMENT='支付对账明细'
                        """);
                ensureReconMenus(st);
                ready = true;
                log.info("支付订单表已就绪");
            } catch (Exception e) {
                throw new IllegalStateException("自动创建支付订单表失败：" + e.getMessage(), e);
            }
        }
    }

    private void ensureReconMenus(Statement st) {
        try {
            st.execute("""
                    INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
                    SELECT 314, '支付对账', 2, 310, 4, '/pay/recon', '/business/pay/pay-recon-list.vue', 'AuditOutlined', 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()
                    WHERE EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 310)
                      AND NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 314)
                    """);
            insertPoint(st, 315, "查询对账", "pay:recon:query", 1);
            insertPoint(st, 316, "拉取账单", "pay:recon:pull", 2);
            insertPoint(st, 317, "上传账单", "pay:recon:upload", 3);
            insertPoint(st, 318, "演示对账", "pay:recon:mock", 4);
            insertPoint(st, 319, "核销差异", "pay:recon:handle", 5);
            insertPoint(st, 320, "导出明细", "pay:recon:export", 6);
            insertPoint(st, 321, "删除批次", "pay:recon:delete", 7);
            st.execute("""
                    INSERT INTO `t_role_menu` (`role_id`, `menu_id`, `create_time`, `update_time`)
                    SELECT 1, m.menu_id, NOW(), NOW()
                    FROM `t_menu` m
                    WHERE m.menu_id IN (314, 315, 316, 317, 318, 319, 320, 321)
                      AND NOT EXISTS (SELECT 1 FROM `t_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id)
                    """);
        } catch (Exception e) {
            log.warn("补齐对账菜单失败（可手工执行 sql-update-log/pay-recon.sql）: {}", e.getMessage());
        }
    }

    private void insertPoint(Statement st, int menuId, String name, String perm, int sort) throws Exception {
        st.execute("INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) "
                + "SELECT " + menuId + ", '" + name + "', 3, 314, " + sort + ", 1, '" + perm + "', '" + perm + "', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW() "
                + "WHERE EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 314) "
                + "AND NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = " + menuId + ")");
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

    private void addIndexIfMissing(Connection conn, String table, String indexName, String columns) {
        try (Statement query = conn.createStatement();
             var rs = query.executeQuery("SHOW INDEX FROM `" + table + "` WHERE Key_name = '" + indexName + "'")) {
            if (rs.next()) {
                return;
            }
        } catch (Exception e) {
            log.warn("检查索引失败 {}.{}: {}", table, indexName, e.getMessage());
            return;
        }
        try (Statement alter = conn.createStatement()) {
            alter.execute("ALTER TABLE `" + table + "` ADD INDEX `" + indexName + "` " + columns);
            log.info("已为 {} 增加索引 {}", table, indexName);
        } catch (Exception e) {
            log.warn("增加索引失败 {}.{}: {}", table, indexName, e.getMessage());
        }
    }
}
