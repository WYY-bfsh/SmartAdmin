# 秒杀商城功能说明

本文档对应近期补齐的 H5 用户端、Java 接口与管理后台。手机短信验证码、忘记密码暂未做。

---

## 1. 注册 / 登录

### 业务规则
- 手机号注册，注册时设置登录密码。
- **邀请码必填**，必须是已有会员的 `invite_code`，用于绑定上级（分销）。
- 登录：手机号 + 密码。
- 未做：管理员发短信验证码、忘记密码。

### H5 页面
| 文件 | 说明 |
|---|---|
| `smart-app/src/pages/mall/register.vue` | 注册。邀请码必填；拦截浏览器把账号误填进邀请码。 |
| `smart-app/src/pages/mall/login.vue` | 账号密码登录。 |
| `smart-app/src/utils/mall-invite.js` | 从链接 `?invite=` 捕获邀请码并写入本地。 |

### 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/mall/h5/register` | 注册。`inviteCode` 不能为空且必须有效。 |
| POST | `/mall/h5/login` | 登录，返回 `Mall-Token`。 |
| POST | `/mall/h5/avatar/upload` | 头像/收款码/付款截图上传。 |

### 数据表
`t_mall_member`：`phone`、`password`（MD5）、`invite_code`（自己的码）、`parent_member_id`（上级）。

演示邀请码：`SA0001`（店长 13800000001 / 123456）。

---

## 2. 未登录访问抢购 / 订单

### 业务规则
点底部 Tab「抢购」「订单」且未登录时：弹出登录框，**不加载列表数据**。点遮罩或「返回首页」回到首页 Tab。

### H5
| 文件 | 说明 |
|---|---|
| `smart-app/src/components/mall-login-popup/index.vue` | 登录弹层。 |
| `smart-app/src/pages/mall/home.vue` | 抢购 Tab：先鉴权，再展示倒计时。 |
| `smart-app/src/pages/mall/order.vue` | 订单 Tab：未登录不展示订单。 |

「我的」允许未登录进入，点击菜单再跳登录。

---

## 3. 秒杀倒计时与预览

### 业务规则（例：9:30–12:30 开售）
1. 点 Tab「抢购」先进入**倒计时页**，不直接出商品列表。
2. 开售前 **30 分钟**（9:00）可点「预览商品」进列表和详情，**不能下单**。
3. 到达开始时间后可「进入抢购」并下单。
4. 场次时间取当前未结束活动中最早 `start_time`、最晚 `end_time`。

活动状态（接口计算）：`10` 未开始 / `20` 进行中 / `30` 已结束或售罄。未开始时后端拒绝下单。

### H5
| 文件 | 说明 |
|---|---|
| `smart-app/src/pages/mall/home.vue` | 倒计时页。 |
| `smart-app/src/pages/mall/seckill-goods.vue` | 商品列表；预览期未到会打回倒计时页。 |
| `smart-app/src/pages/mall/detail.vue` | 详情；未开始按钮为「仅可预览」。 |
| `smart-app/src/utils/mall-seckill.js` | 预览窗口、场次、倒计时格式化。常量 `PREVIEW_MINUTES = 30`。 |

### 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/mall/h5/activity/list` | 上架活动列表（含开始/结束时间、saleStatus）。 |
| GET | `/mall/h5/activity/{id}` | 活动详情。 |
| POST | `/mall/h5/order/create` | 仅 `saleStatus=20` 可下单。 |

后台改活动时间：管理端「秒杀活动」。

---

## 4. 购买与线下支付确认

### 业务规则
- 一单只买 **一种商品**，数量可在库存和每人限购内自选。
- **限购按件数累计**（未关闭订单的 `qty` 之和），不是按下单笔数。
- 下单 → **待付款(10)**：展示订单信息 + **商家**微信/支付宝收款码。至少配置一张收款码才能交凭证。
- 用户上传付款截图、可填说明 → **待商家确认(15)**。提交时订单必须仍是 10 且未超过支付时限。
- 后台核对截图：**确认收款** → **待发货(20)**；**拒绝** → **已关闭(50)** 并回库存，用户可重新下单。
- 超时关单（默认约 15 分钟）只处理仍为 **10** 的订单，用「状态仍为待付款」的条件更新，避免和交凭证抢同一单、误回库存。

会员注册时的收款码是分销用，**不是**待支付页商家码。商家码在「秒杀活动」页上传。

### 订单状态
| 值 | 含义 | 下一步 |
|---|---|---|
| 10 | 待付款 | 交截图；或超时关闭 |
| 15 | 待商家确认 | 后台确认收款或拒绝 |
| 20 | 待发货 | 后台发货 |
| 30 | 已发货 | 用户确认收货 |
| 40 | 已完成 | — |
| 50 | 已关闭 | 超时未付或商家拒绝，库存已回 |

支付状态：10 待支付 / 15 待确认 / 20 已支付 / 30 已关闭。

### H5
| 文件 | 说明 |
|---|---|
| `smart-app/src/pages/mall/detail.vue` | 数量步进器；`qty` 提交下单。 |
| `smart-app/src/pages/mall/order-detail.vue` | 待付款展示收款码、上传截图；无收款码不可提交；关闭态展示原因。 |
| `smart-app/src/pages/mall/order.vue` | 增加「待确认」筛选。 |
| `smart-app/src/api/business/mall/mall-h5-api.js` | `submitPayProof`。 |

### 管理端
| 文件 | 说明 |
|---|---|
| `activity-list.vue` | 上传商家微信/支付宝收款码。 |
| `order-list.vue` | 待确认订单「确认收款 / 拒绝」。 |
| `mall-admin-api.js` | `saveSetting`、`confirmPay`、`rejectPay`。 |

### 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/mall/h5/config` | 含 `merchantWechatQr`、`merchantAlipayQr`。 |
| POST | `/mall/h5/order/create` | 下单，状态 10；限购按 SUM(qty)。 |
| POST | `/mall/h5/order/pay-proof` | 截图+说明；须有商家码、未超时、状态 10 → 15。 |
| POST | `/mall/admin/setting/save` | 保存商家收款码。 |
| POST | `/mall/admin/order/confirm-pay/{orderId}` | 须已有截图，15 → 20。 |
| POST | `/mall/admin/order/reject-pay` | 15 → 50，回库存。 |

---

## 5. 数据表

| 表 | 用途 |
|---|---|
| `t_mall_member` | 会员、邀请码、分销收款码 |
| `t_mall_setting` | 单行配置（`setting_id=1`），商家收款码 |
| `t_mall_order` | 订单；`pay_proof_url` 付款截图，`pay_note` 说明 |
| `t_mall_address` | 收货地址 |
| `t_seckill_activity` | 秒杀活动时间、库存、限购 |
| `t_mall_express_trace` | 物流轨迹 |
| `t_mall_commission` | 一级分销佣金（商家确认收款后冻结） |

表由 `MallSchemaService` 启动时自动创建/补字段。改接口后需**重启 Java**。

---

## 6. 后台菜单

「秒杀商城」：秒杀活动、商城订单、会员、分销佣金。权限点含 `mall:activity:save`（改收款码）、`mall:order:ship`（确认收款、拒绝收款、发货）。

---

## 7. 联调顺序

1. 重启 `AdminApplication`。
2. 后台「秒杀活动」上传两张商家收款码；把活动开始时间设到当前之后（测倒计时）或当前之前（测下单）。
3. H5 用邀请码 `SA0001` 注册或演示账号登录。
4. 抢购 Tab 看倒计时 → 预览窗口内进商品 → 开售后选数量下单。
5. 待付款页扫码，上传截图提交。
6. 后台订单「确认收款」→ 发货；截图不对则点「拒绝」关单回库存。

### 支付实现要点（已按此加固）
- 限购：`SUM(qty)`，未关闭订单累计件数。
- 交凭证与超时关单：SQL 条件更新（必须仍是状态 10），不会一边交凭证一边回库存。
- 无商家收款码：H5 禁用提交，接口也会拒绝。
- 待确认只能后台确认或拒绝，不会被支付超时任务关掉。
