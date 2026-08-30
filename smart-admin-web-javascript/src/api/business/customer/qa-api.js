/*
 * 客服问答
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const qaApi = {
  query: (param) => {
    return postRequest('/customer/qa/query', param);
  },
  detail: (qaId) => {
    return getRequest(`/customer/qa/detail/${qaId}`);
  },
  ask: (param) => {
    return postRequest('/customer/qa/ask', param);
  },
  answer: (param) => {
    return postRequest('/customer/qa/answer', param);
  },
  delete: (qaId) => {
    return getRequest(`/customer/qa/delete/${qaId}`);
  },
};