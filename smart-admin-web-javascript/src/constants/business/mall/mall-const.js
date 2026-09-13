/** 订单状态：10 待付款 → 15 待商家确认 → 20 待发货 → 30 已发货 → 40 已完成 / 50 关闭 */
export const MALL_ORDER_STATUS_ENUM = {
  WAIT_PAY: { value: 10, desc: '待付款' },
  WAIT_CONFIRM: { value: 15, desc: '待商家确认' },
  WAIT_SHIP: { value: 20, desc: '待发货' },
  SHIPPED: { value: 30, desc: '已发货' },
  COMPLETED: { value: 40, desc: '已完成' },
  CLOSED: { value: 50, desc: '已关闭' },
};

export const MALL_PAY_STATUS_ENUM = {
  WAIT_PAY: { value: 10, desc: '待支付' },
  WAIT_CONFIRM: { value: 15, desc: '待确认' },
  PAID: { value: 20, desc: '已支付' },
  CLOSED: { value: 30, desc: '已关闭' },
};

export const COMMISSION_STATUS_ENUM = {
  FROZEN: { value: 10, desc: '待结算' },
  SETTLED: { value: 20, desc: '已结算' },
  CANCELED: { value: 30, desc: '已取消' },
};

export const SECKILL_SALE_STATUS_ENUM = {
  WAIT: { value: 10, desc: '未开始' },
  LIVE: { value: 20, desc: '进行中' },
  END: { value: 30, desc: '已结束' },
};

export default {
  MALL_ORDER_STATUS_ENUM,
  MALL_PAY_STATUS_ENUM,
  COMMISSION_STATUS_ENUM,
  SECKILL_SALE_STATUS_ENUM,
};
