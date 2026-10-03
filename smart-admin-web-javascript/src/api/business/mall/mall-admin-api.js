import { postRequest, getRequest } from '/@/lib/axios';

/** 秒杀商城管理端接口。saveSetting=商家收款码，confirmPay=确认用户付款截图。 */
export const mallAdminApi = {
  config: () => getRequest('/mall/admin/config'),
  saveSetting: (param) => postRequest('/mall/admin/setting/save', param),
  queryActivity: (param) => postRequest('/mall/admin/activity/query', param),
  saveActivity: (param) => postRequest('/mall/admin/activity/save', param),
  queryOrder: (param) => postRequest('/mall/admin/order/query', param),
  orderDetail: (orderId) => getRequest(`/mall/admin/order/${orderId}`),
  confirmPay: (orderId) => postRequest(`/mall/admin/order/confirm-pay/${orderId}`),
  rejectPay: (param) => postRequest('/mall/admin/order/reject-pay', param),
  wechatRefund: (orderId, param) => postRequest(`/mall/admin/order/wechat-refund/${orderId}`, param || {}),
  alipayRefund: (orderId, param) => postRequest(`/mall/admin/order/alipay-refund/${orderId}`, param || {}),
  ship: (param) => postRequest('/mall/admin/order/ship', param),
  companies: () => getRequest('/mall/admin/express/companies'),
  queryMember: (param) => postRequest('/mall/admin/member/query', param),
  updateMember: (param) => postRequest('/mall/admin/member/update', param),
  queryCommission: (param) => postRequest('/mall/admin/commission/query', param),
};
