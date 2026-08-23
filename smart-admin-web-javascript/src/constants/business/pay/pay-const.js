/*
 * 微信支付
 */
export const PAY_STATUS_ENUM = {
  WAIT_PAY: {
    value: 10,
    desc: '待支付',
  },
  SUCCESS: {
    value: 20,
    desc: '支付成功',
  },
  CLOSED: {
    value: 30,
    desc: '已关闭',
  },
  REFUNDING: {
    value: 40,
    desc: '退款中',
  },
  REFUND: {
    value: 50,
    desc: '已退款',
  },
};

export const PAY_TRADE_TYPE_ENUM = {
  NATIVE: {
    value: 1,
    desc: '扫码支付',
  },
};

export default {
  PAY_STATUS_ENUM,
  PAY_TRADE_TYPE_ENUM,
};
