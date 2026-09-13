/**
 * 秒杀商城 H5 接口。Token 放在请求头 Mall-Token。
 * 下单后：微信支付可用则走 wechatPrepay；否则 submitPayProof 收款码截图。
 */
import { MALL_TOKEN } from '@/constants/local-storage-key-const';

function resolveBase() {
  const envUrl = import.meta.env.VITE_APP_API_URL || 'http://127.0.0.1:1024';
  // #ifdef H5
  // 仅本地开发时，手机访问局域网 IP 才改打同机 1024；线上用 .env.production
  if (import.meta.env.DEV && typeof window !== 'undefined') {
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

export function resolveMallFileUrl(url) {
  if (!url) {
    return '';
  }
  if (url.startsWith('data:') || url.startsWith('blob:')) {
    return url;
  }
  const base = resolveBase();
  try {
    const parsed = new URL(url);
    const idx = parsed.pathname.indexOf('/upload/');
    if (idx >= 0 && typeof window !== 'undefined') {
      return `${window.location.origin}${parsed.pathname.substring(idx)}`;
    }
    const baseUrl = new URL(base);
    if (parsed.hostname !== baseUrl.hostname) {
      parsed.hostname = baseUrl.hostname;
      parsed.port = baseUrl.port;
      parsed.protocol = baseUrl.protocol;
    }
    return parsed.toString();
  } catch (e) {
    const path = String(url).replace(/^\//, '');
    return `${base}${path.startsWith('upload/') ? '/' : '/upload/'}${path}`;
  }
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

function handleUploadResult(raw, resolve, reject) {
  let res = {};
  try {
    res = typeof raw === 'object' && raw ? raw : JSON.parse(String(raw || '{}').replace('\uFEFF', ''));
  } catch (e) {
    uni.showToast({ title: '图片上传失败', icon: 'none' });
    reject(e);
    return;
  }
  if (res.code && res.code !== 1) {
    uni.showToast({ title: hintForCode(res), icon: 'none' });
    reject(res);
    return;
  }
  resolve(res);
}

async function pathToH5File(filePath) {
  if (typeof window === 'undefined') {
    return null;
  }
  if (!filePath || !(filePath.startsWith('blob:') || filePath.startsWith('data:') || filePath.startsWith('http'))) {
    return null;
  }
  const res = await fetch(filePath);
  const blob = await res.blob();
  let ext = 'jpg';
  if (blob.type && blob.type.includes('/')) {
    ext = blob.type.split('/')[1].replace('jpeg', 'jpg');
  }
  const type = blob.type && blob.type.startsWith('image/') ? blob.type : 'image/jpeg';
  return new File([blob], `qrcode.${ext}`, { type });
}

function mallUpload(url, filePath) {
  return new Promise((resolve, reject) => {
    const uploadUrl = resolveBase() + url;
    // #ifdef H5
    pathToH5File(filePath)
      .then((file) => {
        if (!file) {
          return Promise.reject(new Error('no-h5-file'));
        }
        const formData = new FormData();
        formData.append('file', file, file.name);
        return fetch(uploadUrl, {
          method: 'POST',
          headers: {
            'Mall-Token': getMallToken(),
          },
          body: formData,
        }).then((response) => response.json());
      })
      .then((res) => handleUploadResult(res, resolve, reject))
      .catch((e) => {
        if (e && e.message === 'no-h5-file') {
          nativeUpload(uploadUrl, filePath, resolve, reject);
          return;
        }
        uni.showToast({ title: (e && e.msg) || '图片上传失败', icon: 'none' });
        reject(e);
      });
    return;
    // #endif
    nativeUpload(uploadUrl, filePath, resolve, reject);
  });
}

function nativeUpload(uploadUrl, filePath, resolve, reject) {
  uni.uploadFile({
    url: uploadUrl,
    filePath,
    name: 'file',
    header: {
      'Mall-Token': getMallToken(),
    },
    success: (response) => {
      handleUploadResult(response.data, resolve, reject);
    },
    fail: () => {
      uni.showToast({
        title: '图片上传失败，请确认后端 1024 可访问',
        icon: 'none',
      });
      reject(new Error('network'));
    },
  });
}

export const mallH5Api = {
  config: () => mallRequest('/mall/h5/config', 'GET'),
  uploadAvatar: (filePath) => mallUpload('/mall/h5/avatar/upload', filePath),
  register: (data) => mallRequest('/mall/h5/register', 'POST', data),
  login: (data) => mallRequest('/mall/h5/login', 'POST', data),
  me: () => mallRequest('/mall/h5/me', 'GET'),
  activityList: () => mallRequest('/mall/h5/activity/list', 'GET'),
  activityDetail: (id) => mallRequest(`/mall/h5/activity/${id}`, 'GET'),
  addressList: () => mallRequest('/mall/h5/address/list', 'GET'),
  saveAddress: (data) => mallRequest('/mall/h5/address/save', 'POST', data),
  createOrder: (data) => mallRequest('/mall/h5/order/create', 'POST', data),
  wechatPrepay: (data) => mallRequest('/mall/h5/order/wechat/prepay', 'POST', data),
  wechatMockPay: (orderId) => mallRequest(`/mall/h5/order/wechat/mock-pay/${orderId}`, 'POST'),
  wechatOauthUrl: (redirectUri, state) =>
    mallRequest('/mall/h5/wechat/oauth-url', 'GET', { redirectUri, state }),
  wechatOauth: (code) => mallRequest('/mall/h5/wechat/oauth', 'GET', { code }),
  alipayPrepay: (data) => mallRequest('/mall/h5/order/alipay/prepay', 'POST', data),
  alipayMockPay: (orderId) => mallRequest(`/mall/h5/order/alipay/mock-pay/${orderId}`, 'POST'),
  submitPayProof: (data) => mallRequest('/mall/h5/order/pay-proof', 'POST', data),
  orderList: (orderStatus) =>
    mallRequest('/mall/h5/order/list', 'GET', orderStatus == null ? {} : { orderStatus }),
  orderDetail: (orderId) => mallRequest(`/mall/h5/order/${orderId}`, 'GET'),
  receive: (orderId) => mallRequest(`/mall/h5/order/receive/${orderId}`, 'POST'),
  commission: (data) => mallRequest('/mall/h5/commission/query', 'POST', data),
  team: () => mallRequest('/mall/h5/team', 'GET'),
};

export const MALL_ORDER_STATUS = {
  10: '待付款',
  15: '待商家确认',
  20: '待发货',
  30: '已发货',
  40: '已完成',
  50: '已关闭',
};
