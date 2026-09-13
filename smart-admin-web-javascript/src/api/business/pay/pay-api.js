/*
 * 微信支付
 */
import { postRequest, getRequest, getDownload } from '/@/lib/axios';

export const payApi = {
  getConfig: () => {
    return getRequest('/pay/wechat/config');
  },
  getAlipayConfig: () => {
    return getRequest('/pay/alipay/config');
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
  mockPay: (payOrderId) => {
    return postRequest(`/pay/order/mock-pay/${payOrderId}`);
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
  queryReconBatch: (param) => {
    return postRequest('/pay/recon/batch/query', param);
  },
  reconBatchDetail: (batchId) => {
    return getRequest(`/pay/recon/batch/${batchId}`);
  },
  queryReconItem: (param) => {
    return postRequest('/pay/recon/item/query', param);
  },
  pullRecon: (param) => {
    return postRequest('/pay/recon/pull', param);
  },
  mockRecon: (param) => {
    return postRequest('/pay/recon/mock', param);
  },
  uploadRecon: (formData) => {
    return postRequest('/pay/recon/upload', formData);
  },
  handleRecon: (param) => {
    return postRequest('/pay/recon/handle', param);
  },
  deleteReconBatch: (batchId) => {
    return getRequest(`/pay/recon/batch/delete/${batchId}`);
  },
  exportReconItem: (batchId) => {
    return getDownload(`/pay/recon/item/export/${batchId}`);
  },
};
