/*
 * 客服知识库
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const knowledgeApi = {
  query: (param) => {
    return postRequest('/customer/knowledge/query', param);
  },
  detail: (knowledgeId) => {
    return getRequest(`/customer/knowledge/detail/${knowledgeId}`);
  },
  add: (param) => {
    return postRequest('/customer/knowledge/add', param);
  },
  update: (param) => {
    return postRequest('/customer/knowledge/update', param);
  },
  delete: (knowledgeId) => {
    return getRequest(`/customer/knowledge/delete/${knowledgeId}`);
  },
};