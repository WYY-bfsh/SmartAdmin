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
  JSAPI: {
    value: 2,
    desc: '公众号/JSAPI',
  },
  H5: {
    value: 3,
    desc: '手机H5',
  },
  PAGE: {
    value: 4,
    desc: '电脑网站',
  },
};

export const PAY_CHANNEL_ENUM = {
  WECHAT: {
    value: 1,
    desc: '微信支付',
  },
  ALIPAY: {
    value: 2,
    desc: '支付宝',
  },
};

export const PAY_RECON_MATCH_STATUS_ENUM = {
  MATCHED: { value: 10, desc: '完全匹配' },
  AMOUNT_DIFF: { value: 20, desc: '金额不符' },
  STATUS_DIFF: { value: 30, desc: '状态不符' },
  LOCAL_ONLY: { value: 40, desc: '仅本地有' },
  CHANNEL_ONLY: { value: 50, desc: '仅渠道有' },
};

export const PAY_RECON_SOURCE_ENUM = {
  PULL: { value: 1, desc: '渠道拉取' },
  UPLOAD: { value: 2, desc: '文件上传' },
  MOCK: { value: 3, desc: '演示对账' },
};

export const PAY_RECON_BATCH_STATUS_ENUM = {
  RUNNING: { value: 10, desc: '处理中' },
  DONE: { value: 20, desc: '已完成' },
  FAIL: { value: 30, desc: '失败' },
};

export const PAY_RECON_BIZ_TYPE_ENUM = {
  TRADE: { value: 1, desc: '交易' },
  REFUND: { value: 2, desc: '退款' },
};

export default {
  PAY_STATUS_ENUM,
  PAY_TRADE_TYPE_ENUM,
  PAY_CHANNEL_ENUM,
  PAY_RECON_MATCH_STATUS_ENUM,
  PAY_RECON_SOURCE_ENUM,
  PAY_RECON_BATCH_STATUS_ENUM,
  PAY_RECON_BIZ_TYPE_ENUM,
};
