/*
 * 客服中心 - 枚举常量
 */
export const TICKET_TYPE_ENUM = {
  QUESTION: { value: 1, desc: '问题咨询' },
  SUGGESTION: { value: 2, desc: '功能建议' },
  BUG: { value: 3, desc: 'Bug反馈' },
  BIZ_QUERY: { value: 4, desc: '业务查询' },
  OTHER: { value: 5, desc: '其他' },
};

export const TICKET_PRIORITY_ENUM = {
  LOW: { value: 1, desc: '低' },
  MEDIUM: { value: 2, desc: '中' },
  HIGH: { value: 3, desc: '高' },
  URGENT: { value: 4, desc: '紧急' },
};

export const TICKET_STATUS_ENUM = {
  WAIT_HANDLE: { value: 10, desc: '待处理' },
  HANDLING: { value: 20, desc: '处理中' },
  REPLIED: { value: 30, desc: '已回复' },
  CLOSED: { value: 40, desc: '已关闭' },
};

export const TICKET_MESSAGE_TYPE_ENUM = {
  CUSTOMER: { value: 1, desc: '客户消息' },
  SERVICE: { value: 2, desc: '客服回复' },
  SYSTEM: { value: 3, desc: '系统消息' },
};

export const KNOWLEDGE_CATEGORY_ENUM = {
  FAQ: { value: 1, desc: '常见问题' },
  TUTORIAL: { value: 2, desc: '使用教程' },
  BIZ_DESC: { value: 3, desc: '业务说明' },
  OTHER: { value: 4, desc: '其他' },
};

export const QA_STATUS_ENUM = {
  WAIT_ANSWER: { value: 10, desc: '待回答' },
  ANSWERED: { value: 20, desc: '已回答' },
  REJECTED: { value: 30, desc: '已驳回' },
};

export default {
  TICKET_TYPE_ENUM,
  TICKET_PRIORITY_ENUM,
  TICKET_STATUS_ENUM,
  TICKET_MESSAGE_TYPE_ENUM,
  KNOWLEDGE_CATEGORY_ENUM,
  QA_STATUS_ENUM,
};