# 秒杀商城 H5 页面说明

用户端（uni-app）页面与近期逻辑对应关系。接口与表结构见后端模块  
`sa-admin/.../module/business/mall/README.md`。

## Tab 与页面

| 路由 | 文件 | 职责 |
|---|---|---|
| `pages/mall/index` | `index.vue` | 品牌首页：头图、秒杀/订单快捷入口、公告与企业介绍，无需登录。 |
| `pages/mall/home` | `home.vue` | **抢购 Tab**：未登录弹登录框；已登录显示开售倒计时，30 分钟预览窗口后进入商品页。 |
| `pages/mall/order` | `order.vue` | **订单 Tab**：未登录弹登录框；已登录列订单（含待确认 15）。 |
| `pages/mall/mine` | `mine.vue` | 我的；未登录可进，点菜单再登录。 |
| `pages/mall/seckill-goods` | `seckill-goods.vue` | 秒杀商品列表（非 Tab）。 |
| `pages/mall/detail` | `detail.vue` | 商品详情：预览不可买；开售后可选数量下单。 |
| `pages/mall/order-detail` | `order-detail.vue` | 待付款展示商家收款码、上传付款凭证（无码不可交）；待确认等待审核；关闭态展示原因。 |
| `pages/mall/login` | `login.vue` | 全屏登录（我的入口等）。 |
| `pages/mall/register` | `register.vue` | 邀请码必填注册；防浏览器误填邀请码。 |

## 公共组件 / 工具

| 文件 | 职责 |
|---|---|
| `src/components/mall-login-popup/index.vue` | 抢购/订单未登录弹层。 |
| `src/utils/mall-invite.js` | 邀请码从 URL 带入注册页。 |
| `src/utils/mall-seckill.js` | 预览 30 分钟、场次时间、倒计时文案。 |
| `src/api/business/mall/mall-h5-api.js` | H5 接口；请求头 `Mall-Token`。 |

## 请求头

登录/注册成功后 token 存本地，后续请求带 `Mall-Token`。

## 支付注意
- 待付款超时后提交凭证会提示已关闭。
- 后台拒绝后订单为已关闭，库存退回，可重新抢。
- 活动「每人限购」按累计件数，演示数据若为 1 则数量无法加。

