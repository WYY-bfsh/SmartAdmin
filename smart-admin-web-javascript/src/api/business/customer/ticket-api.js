/*
 * 客服工单
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const ticketApi = {
  query: (param) => {
    return postRequest('/customer/ticket/query', param);
  },
  detail: (ticketId) => {
    return getRequest(`/customer/ticket/detail/${ticketId}`);
  },
  create: (param) => {
    return postRequest('/customer/ticket/create', param);
  },
  reply: (param) => {
    return postRequest('/customer/ticket/reply', param);
  },
  close: (ticketId) => {
    return getRequest(`/customer/ticket/close/${ticketId}`);
  },
  delete: (ticketId) => {
    return getRequest(`/customer/ticket/delete/${ticketId}`);
  },
};