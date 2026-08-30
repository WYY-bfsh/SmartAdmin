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
                ready = true;
                log.info("支付订单表已就绪");
            } catch (Exception e) {
                throw new IllegalStateException("自动创建支付订单表失败：" + e.getMessage(), e);
            }
        }
    }
}
