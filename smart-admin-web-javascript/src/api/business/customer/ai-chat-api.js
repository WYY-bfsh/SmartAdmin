import { getRequest, postRequest } from '/@/lib/axios';

export const aiChatApi = {
  saveHistory: (content) => {
    return postRequest('/customer/ai/chat/history/save', { content });
  },
  getHistory: () => {
    return getRequest('/customer/ai/chat/history/get');
  },
};
