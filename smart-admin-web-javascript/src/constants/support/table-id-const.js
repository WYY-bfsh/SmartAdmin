/*
 * @Description: 表格id
 * @Author: zhuoda
 * @Date: 2022-08-21
 * @LastEditTime: 2022-08-21
 * @LastEditors: zhuoda
 */

//system系统功能表格初始化id
let systemInitTableId = 10000;

//support支撑功能表格初始化id
let supportInitTableId = 20000;

//业务表格初始化id
let businessOAInitTableId = 30000;

let businessERPInitTableId = 40000;
let businessMediaInitTableId = 50000;
let businessCustomerInitTableId = 60000;
let businessMallInitTableId = 70000;

export const TABLE_ID_CONST = {
  /**
   * 业务
   */
  BUSINESS: {
    OA: {
      NOTICE: businessOAInitTableId + 1, //通知公告
      ENTERPRISE: businessOAInitTableId + 2, //企业信息
      ENTERPRISE_EMPLOYEE: businessOAInitTableId + 3, //企业员工
      ENTERPRISE_BANK: businessOAInitTableId + 4, //企业银行
      ENTERPRISE_INVOICE: businessOAInitTableId + 5, //企业发票
    },
    ERP: {
      GOODS: businessERPInitTableId + 1, //商品管理
    },
    PAY: {
      ORDER: businessERPInitTableId + 11, //微信支付订单
      RECON_BATCH: businessERPInitTableId + 12,
      RECON_ITEM: businessERPInitTableId + 13,
    },
    MEDIA: {
      AI_PROJECT: businessMediaInitTableId + 1,
      AI_MATERIAL: businessMediaInitTableId + 2,
      AI_TASK: businessMediaInitTableId + 3,
      MUSIC_SONG: businessMediaInitTableId + 4,
      YINGYUE_VIDEO: businessMediaInitTableId + 5,
    },
    CUSTOMER: {
      TICKET: businessCustomerInitTableId + 1,
      KNOWLEDGE: businessCustomerInitTableId + 2,
      QA: businessCustomerInitTableId + 3,
    },
    MALL: {
      ACTIVITY: businessMallInitTableId + 1,
      ORDER: businessMallInitTableId + 2,
      MEMBER: businessMallInitTableId + 3,
      COMMISSION: businessMallInitTableId + 4,
    },
  },

  /**
   * 系统
   */
  SYSTEM: {
    EMPLOYEE: systemInitTableId + 1, //员工
    MENU: systemInitTableId + 2, //菜单
    POSITION: systemInitTableId + 3, //职位
  },
  /**
   * 支撑
   */
  SUPPORT: {
    CONFIG: supportInitTableId + 1, //参数配置
    DICT: supportInitTableId + 2, //字典
    SERIAL_NUMBER: supportInitTableId + 3, //单号
    OPERATE_LOG: supportInitTableId + 4, //请求监控
    HEART_BEAT: supportInitTableId + 5, //心跳
    LOGIN_LOG: supportInitTableId + 6, //登录日志
    RELOAD: supportInitTableId + 7, //reload
    HELP_DOC: supportInitTableId + 8, //帮助文档
    JOB: supportInitTableId + 9, //Job
    JOB_LOG: supportInitTableId + 10, //JobLog
    MAIL: supportInitTableId + 11,
  },
};