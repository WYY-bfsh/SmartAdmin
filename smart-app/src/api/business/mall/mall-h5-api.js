import { MALL_TOKEN } from '@/constants/local-storage-key-const';

function resolveBase() {
  const envUrl = import.meta.env.VITE_APP_API_URL || 'http://127.0.0.1:1024';
  // #ifdef H5
  if (typeof window !== 'undefined') {
    const host = window.location.hostname;
    if (host && host !== 'localhost' && host !== '127.0.0.1') {
      return `http://${host}:1024`;
    }
  }
  // #endif
  return envUrl;
}

export function getMallToken() {
  return uni.getStorageSync(MALL_TOKEN) || '';
}

export function saveMallToken(token) {
  if (token) {
    uni.setStorageSync(MALL_TOKEN, token);
  }
}

export function clearMallToken() {
  uni.removeStorageSync(MALL_TOKEN);
}

function hintForCode(res) {
  if (res && res.code === 10001) {
    return '秒杀接口未加载：请在 IDEA 停止 Java，再重新运行 AdminApplication';
  }
  return (res && res.msg) || '请求失败';
}

function mallRequest(url, method, data) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: resolveBase() + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Mall-Token': getMallToken(),
      },
      success: (response) => {
        const res = response.data || {};
        if (res.code && res.code !== 1) {
          if (res.code === 30007 || res.code === 30008 || res.code === 30012) {
            clearMallToken();
            const pages = getCurrentPages();
            const current = pages[pages.length - 1];
            const route = current ? '/' + current.route : '';
            if (route !== '/pages/mall/login') {
              uni.navigateTo({ url: '/pages/mall/login' });
            }
          }
          uni.showToast({ title: hintForCode(res), icon: 'none' });
          reject(res);
          return;
        }
        resolve(res);
      },
      fail: () => {
        uni.showToast({
          title: '连不上后端 1024，请确认 AdminApplication 已启动',
          icon: 'none',
        });
        reject(new Error('network'));
      },
    });
  });
}

export const mallH5Api = {
  config: () => mallRequest('/mall/h5/config', 'GET'),
  register: (data) => mallRequest('/mall/h5/register', 'POST', data),
  login: (data) => mallRequest('/mall/h5/login', 'POST', data),
  me: () => mallRequest('/mall/h5/me', 'GET'),
  activityList: () => mallRequest('/mall/h5/activity/list', 'GET'),
  activityDetail: (id) => mallRequest(`/mall/h5/activity/${id}`, 'GET'),
  addressList: () => mallRequest('/mall/h5/address/list', 'GET'),
  saveAddress: (data) => mallRequest('/mall/h5/address/save', 'POST', data),
  createOrder: (data) => mallRequest('/mall/h5/order/create', 'POST', data),
  pay: (orderId) => mallRequest(`/mall/h5/order/pay/${orderId}`, 'POST'),
  orderList: (orderStatus) =>
    mallRequest('/mall/h5/order/list', 'GET', orderStatus == null ? {} : { orderStatus }),
  orderDetail: (orderId) => mallRequest(`/mall/h5/order/${orderId}`, 'GET'),
  receive: (orderId) => mallRequest(`/mall/h5/order/receive/${orderId}`, 'POST'),
  commission: (data) => mallRequest('/mall/h5/commission/query', 'POST', data),
  team: () => mallRequest('/mall/h5/team', 'GET'),
};

export const MALL_ORDER_STATUS = {
  10: '待付款',
  20: '待发货',
  30: '已发货',
  40: '已完成',
  50: '已关闭',
};
