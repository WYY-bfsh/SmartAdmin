/*
 * 微信支付
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const payApi = {
  getConfig: () => {
    return getRequest('/pay/wechat/config');
  },
  queryOrder: (param) => {
    return postRequest('/pay/order/query', param);
  },
  detail: (payOrderId) => {
    return getRequest(`/pay/order/detail/${payOrderId}`);
  },
  create: (param) => {
    return postRequest('/pay/order/create', param);
  },
  qrcode: (payOrderId) => {
    return getRequest(`/pay/order/qrcode/${payOrderId}`);
  },
  sync: (payOrderId) => {
    return getRequest(`/pay/order/sync/${payOrderId}`);
  },
  close: (payOrderId) => {
    return getRequest(`/pay/order/close/${payOrderId}`);
  },
  refund: (param) => {
    return postRequest('/pay/order/refund', param);
  },
};
