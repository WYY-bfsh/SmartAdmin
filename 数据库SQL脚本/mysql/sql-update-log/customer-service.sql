-- 客服中心：工单 + 知识库 + 菜单
-- 执行库：smart_admin_v3

-- ----------------------------
-- 1. 工单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_cs_ticket` (
  `ticket_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `ticket_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单编号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单标题',
  `ticket_type` int(0) NOT NULL DEFAULT 1 COMMENT '类型：1问题咨询 2功能建议 3Bug反馈 4业务查询 5其他',
  `priority` int(0) NOT NULL DEFAULT 2 COMMENT '优先级：1低 2中 3高 4紧急',
  `status` int(0) NOT NULL DEFAULT 10 COMMENT '状态：10待处理 20处理中 30已回复 40已关闭',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单内容',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `create_user_id` bigint(0) NULL DEFAULT NULL COMMENT '创建人（员工ID）',
  `handler_user_id` bigint(0) NULL DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `close_time` datetime(0) NULL DEFAULT NULL COMMENT '关闭时间',
  `reply_count` int(0) NOT NULL DEFAULT 0 COMMENT '回复次数',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`ticket_id`) USING BTREE,
  UNIQUE KEY `uk_ticket_no` (`ticket_no`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_user` (`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '客服工单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 工单回复表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_cs_ticket_message` (
  `message_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `ticket_id` bigint(0) NOT NULL COMMENT '工单ID',
  `message_type` int(0) NOT NULL DEFAULT 1 COMMENT '类型：1客户消息 2客服回复 3系统消息',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `create_user_id` bigint(0) NULL DEFAULT NULL COMMENT '发送人（员工ID）',
  `create_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送人名称',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  KEY `idx_ticket_id` (`ticket_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单回复消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 知识库表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_cs_knowledge` (
  `knowledge_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `category` int(0) NOT NULL DEFAULT 1 COMMENT '分类：1常见问题 2使用教程 3业务说明 4其他',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容（富文本）',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `view_count` int(0) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_user_id` bigint(0) NULL DEFAULT NULL COMMENT '创建人',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`knowledge_id`) USING BTREE,
  KEY `idx_category` (`category`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '客服知识库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 菜单：客服中心
-- ----------------------------
-- 目录
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 500, '客服中心', 1, 0, 5, '/customer', NULL, NULL, NULL, NULL, 'CustomerServiceOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 500);

-- 工单管理
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 501, '工单管理', 2, 500, 1, '/customer/ticket', '/business/customer/ticket/ticket-list.vue', NULL, NULL, NULL, 'FileProtectOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 501);

-- 知识库
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 502, '知识库', 2, 500, 2, '/customer/knowledge', '/business/customer/knowledge/knowledge-list.vue', NULL, NULL, NULL, 'BookOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 502);

-- 信息查询
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 503, '信息查询', 2, 500, 3, '/customer/info-query', '/business/customer/info-query/info-query.vue', NULL, NULL, NULL, 'SearchOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 503);

-- 功能点：工单查询
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 504, '查询工单', 3, 501, 1, NULL, NULL, 1, 'cs:ticket:query', 'cs:ticket:query', NULL, 501, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 504);

-- 功能点：创建工单
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 505, '创建工单', 3, 501, 2, NULL, NULL, 1, 'cs:ticket:create', 'cs:ticket:create', NULL, 501, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 505);

-- 功能点：回复工单
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 506, '回复工单', 3, 501, 3, NULL, NULL, 1, 'cs:ticket:reply', 'cs:ticket:reply', NULL, 501, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 506);

-- 功能点：关闭工单
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 507, '关闭工单', 3, 501, 4, NULL, NULL, 1, 'cs:ticket:close', 'cs:ticket:close', NULL, 501, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 507);

-- 功能点：删除工单
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 508, '删除工单', 3, 501, 5, NULL, NULL, 1, 'cs:ticket:delete', 'cs:ticket:delete', NULL, 501, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 508);

-- 功能点：知识库管理
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 509, '查询知识', 3, 502, 1, NULL, NULL, 1, 'cs:knowledge:query', 'cs:knowledge:query', NULL, 502, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 509);

-- 功能点：知识库编辑
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 510, '编辑知识', 3, 502, 2, NULL, NULL, 1, 'cs:knowledge:edit', 'cs:knowledge:edit', NULL, 502, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 510);

-- 功能点：信息查询
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`)
SELECT 511, '查询信息', 3, 503, 1, NULL, NULL, 1, 'cs:info:query', 'cs:info:query', NULL, 503, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `t_menu` WHERE `menu_id` = 511);

-- ----------------------------
-- 5. 演示数据
-- ----------------------------
-- 演示工单
INSERT INTO `t_cs_ticket` (`ticket_no`, `title`, `ticket_type`, `priority`, `status`, `content`, `contact_name`, `contact_phone`, `contact_email`, `create_user_id`, `handler_user_id`, `handle_time`, `close_time`, `reply_count`, `deleted_flag`, `create_time`, `update_time`)
SELECT 'CS20260823000001', '演示-系统登录问题咨询', 1, 2, 20, '请问如何重置密码？我忘记了登录密码，尝试多次都失败，账号被锁定了。', '张三', '13800138001', 'zhangsan@example.com', NULL, NULL, NULL, NULL, 0, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM `t_cs_ticket` WHERE `ticket_no` = 'CS20260823000001');

INSERT INTO `t_cs_ticket` (`ticket_no`, `title`, `ticket_type`, `priority`, `status`, `content`, `contact_name`, `contact_phone`, `contact_email`, `create_user_id`, `handler_user_id`, `handle_time`, `close_time`, `reply_count`, `deleted_flag`, `create_time`, `update_time`)
SELECT 'CS20260822000002', '演示-功能建议：增加批量导出', 2, 3, 10, '建议在订单管理页面增加批量导出Excel的功能，方便我们月底对账。', '李四', '13800138002', 'lisi@example.com', NULL, NULL, NULL, NULL, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `t_cs_ticket` WHERE `ticket_no` = 'CS20260822000002');

INSERT INTO `t_cs_ticket` (`ticket_no`, `title`, `ticket_type`, `priority`, `status`, `content`, `contact_name`, `contact_phone`, `contact_email`, `create_user_id`, `handler_user_id`, `handle_time`, `close_time`, `reply_count`, `deleted_flag`, `create_time`, `update_time`)
SELECT 'CS20260821000003', '演示-已处理的支付问题', 4, 4, 40, '客户反映支付成功后订单状态未更新，需要紧急处理。', '王五', '13800138003', 'wangwu@example.com', 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `t_cs_ticket` WHERE `ticket_no` = 'CS20260821000003');

-- 演示知识库文章
INSERT INTO `t_cs_knowledge` (`title`, `category`, `content`, `sort`, `view_count`, `create_user_id`, `deleted_flag`, `create_time`, `update_time`)
SELECT '如何重置登录密码？', 1, '<p>如忘记密码，请按以下步骤操作：</p><ol><li>点击登录页面的"忘记密码"链接</li><li>输入您的注册手机号或邮箱</li><li>接收验证码并验证</li><li>设置新密码（至少8位，包含字母和数字）</li><li>使用新密码登录系统</li></ol><p>如仍无法重置，请联系管理员或提交工单。</p>', 1, 128, 1, 0, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `t_cs_knowledge` WHERE `title` = '如何重置登录密码？');

INSERT INTO `t_cs_knowledge` (`title`, `category`, `content`, `sort`, `view_count`, `create_user_id`, `deleted_flag`, `create_time`, `update_time`)
SELECT '系统功能介绍总览', 2, '<h3>SmartAdmin 功能模块</h3><ul><li><strong>系统管理</strong>：员工、部门、角色、菜单、岗位管理</li><li><strong>ERP进销存</strong>：商品分类、商品管理</li><li><strong>OA办公</strong>：企业、公告、银行卡、发票管理</li><li><strong>支付模块</strong>：微信支付订单管理</li><li><strong>客服中心</strong>：工单管理、知识库</li></ul><p>更多功能请查看左侧菜单导航。</p>', 2, 256, 1, 0, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `t_cs_knowledge` WHERE `title` = '系统功能介绍总览');

INSERT INTO `t_cs_knowledge` (`title`, `category`, `content`, `sort`, `view_count`, `create_user_id`, `deleted_flag`, `create_time`, `update_time`)
SELECT '微信支付接入流程说明', 3, '<h3>微信支付配置步骤</h3><ol><li>在微信商户平台申请商户号</li><li>配置APIv3密钥和证书</li><li>在系统的"商户配置"页面填写商户信息</li><li>配置支付回调地址</li><li>测试扫码支付功能</li></ol><p>详细配置请参考微信支付官方文档。</p>', 3, 89, 1, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `t_cs_knowledge` WHERE `title` = '微信支付接入流程说明');