import { postRequest, getRequest } from '/@/lib/axios';

export const mallAdminApi = {
  config: () => getRequest('/mall/admin/config'),
  queryActivity: (param) => postRequest('/mall/admin/activity/query', param),
  saveActivity: (param) => postRequest('/mall/admin/activity/save', param),
  queryOrder: (param) => postRequest('/mall/admin/order/query', param),
  orderDetail: (orderId) => getRequest(`/mall/admin/order/${orderId}`),
  ship: (param) => postRequest('/mall/admin/order/ship', param),
  companies: () => getRequest('/mall/admin/express/companies'),
  queryMember: (param) => postRequest('/mall/admin/member/query', param),
  queryCommission: (param) => postRequest('/mall/admin/commission/query', param),
};
