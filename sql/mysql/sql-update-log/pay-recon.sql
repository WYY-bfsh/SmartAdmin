-- 支付对账：批次/明细表 + 菜单功能点
-- 执行库：smart_admin_v3
-- 可重复执行

CREATE TABLE IF NOT EXISTS `t_pay_recon_batch` (
  `batch_id` bigint NOT NULL AUTO_INCREMENT COMMENT '对账批次ID',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `pay_channel` int NOT NULL COMMENT '支付渠道 1微信 2支付宝',
  `source_type` int NOT NULL COMMENT '来源 1拉取 2上传 3演示',
  `batch_status` int NOT NULL DEFAULT 10 COMMENT '10处理中 20完成 30失败',
  `local_count` int NOT NULL DEFAULT 0,
  `channel_count` int NOT NULL DEFAULT 0,
  `matched_count` int NOT NULL DEFAULT 0,
  `amount_diff_count` int NOT NULL DEFAULT 0,
  `status_diff_count` int NOT NULL DEFAULT 0,
  `local_only_count` int NOT NULL DEFAULT 0,
  `channel_only_count` int NOT NULL DEFAULT 0,
  `local_amount` int NOT NULL DEFAULT 0 COMMENT '分',
  `channel_amount` int NOT NULL DEFAULT 0 COMMENT '分',
  `file_name` varchar(255) DEFAULT NULL,
  `error_msg` varchar(500) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_user_id` bigint DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`batch_id`),
  KEY `idx_bill_date_channel` (`bill_date`, `pay_channel`)
) COMMENT='支付对账批次';

CREATE TABLE IF NOT EXISTS `t_pay_recon_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL,
  `match_status` int NOT NULL COMMENT '10匹配 20金额不符 30状态不符 40仅本地 50仅渠道',
  `biz_type` int NOT NULL DEFAULT 1 COMMENT '1交易 2退款',
  `pay_order_id` bigint DEFAULT NULL,
  `order_no` varchar(64) DEFAULT NULL,
  `local_amount` int DEFAULT NULL,
  `local_status` int DEFAULT NULL,
  `channel_trade_no` varchar(64) DEFAULT NULL,
  `channel_order_no` varchar(64) DEFAULT NULL,
  `channel_amount` int DEFAULT NULL,
  `channel_status` varchar(64) DEFAULT NULL,
  `channel_time` varchar(64) DEFAULT NULL,
  `diff_amount` int DEFAULT NULL,
  `handled_flag` tinyint(1) NOT NULL DEFAULT 0,
  `remark` varchar(500) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_match_status` (`match_status`),
  KEY `idx_order_no` (`order_no`)
) COMMENT='支付对账明细';

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `icon`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 314, '支付对账', 2, 310, 4, '/pay/recon', '/business/pay/pay-recon-list.vue', 'AuditOutlined', 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 314);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 315, '查询对账', 3, 314, 1, 1, 'pay:recon:query', 'pay:recon:query', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 315);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 316, '拉取账单', 3, 314, 2, 1, 'pay:recon:pull', 'pay:recon:pull', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 316);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 317, '上传账单', 3, 314, 3, 1, 'pay:recon:upload', 'pay:recon:upload', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 317);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 318, '演示对账', 3, 314, 4, 1, 'pay:recon:mock', 'pay:recon:mock', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 318);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 319, '核销差异', 3, 314, 5, 1, 'pay:recon:handle', 'pay:recon:handle', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 319);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 320, '导出明细', 3, 314, 6, 1, 'pay:recon:export', 'pay:recon:export', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 320);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `perms_type`, `api_perms`, `web_perms`, `visible_flag`, `disabled_flag`, `deleted_flag`, `frame_flag`, `cache_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 321, '删除批次', 3, 314, 7, 1, 'pay:recon:delete', 'pay:recon:delete', 1, 0, 0, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 321);

INSERT INTO `t_role_menu` (`role_id`, `menu_id`, `create_time`, `update_time`)
SELECT 1, m.menu_id, NOW(), NOW()
FROM `t_menu` m
WHERE m.menu_id IN (314, 315, 316, 317, 318, 319, 320, 321)
AND NOT EXISTS (SELECT 1 FROM `t_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id);
