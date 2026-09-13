/*
 * 首页路由
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-06 20:51:41
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
import SmartLayout from '/@/layout/index.vue';

export const homeRouters = [
  {
    path: '/',
    name: '_home',
    redirect: { name: HOME_PAGE_NAME },
    component: SmartLayout,
    meta: {
      title: '首页',
      menuType: MENU_TYPE_ENUM.CATALOG.value,
      icon: 'HomeOutlined',
    },
    children: [
      {
        path: '/home',
        name: HOME_PAGE_NAME,
        meta: {
          title: '首页',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'HomeOutlined',
          parentMenuList: [{ name: '_home', title: '首页' }],
        },
        component: () => import('/@/views/system/home/index.vue'),
      },
      {
        path: '/account',
        name: 'Account',
        component: () => import('/@/views/system/account/index.vue'),
        meta: {
          title: '个人中心',
          hideInMenu: false,
        },
      },
      {
        path: '/pay/order',
        name: '301',
        component: () => import('/@/views/business/pay/pay-order-list.vue'),
        meta: {
          title: '支付订单',
          icon: 'AccountBookOutlined',
        },
      },
      {
        path: '/pay/config',
        name: '302',
        component: () => import('/@/views/business/pay/wechat-pay-config.vue'),
        meta: {
          title: '商户配置',
          icon: 'SettingOutlined',
        },
      },
      {
        path: '/pay/alipay-config',
        name: '313',
        component: () => import('/@/views/business/pay/alipay-pay-config.vue'),
        meta: {
          title: '支付宝配置',
          icon: 'AlipayCircleOutlined',
        },
      },
      {
        path: '/pay/recon',
        name: '314',
        component: () => import('/@/views/business/pay/pay-recon-list.vue'),
        meta: {
          title: '支付对账',
          icon: 'AuditOutlined',
        },
      },
      { path: '/media/ai-clip/workspace', name: '411', component: () => import('/@/views/business/media/ai-clip/workspace.vue'), meta: { title: 'AI漫剪工作台' } },
      { path: '/media/ai-clip/project', name: '412', component: () => import('/@/views/business/media/ai-clip/project-list.vue'), meta: { title: '我的作品' } },
      { path: '/media/ai-clip/project/editor/:id', name: '413', component: () => import('/@/views/business/media/ai-clip/project-editor.vue'), meta: { title: '作品剪辑', hideInMenu: true } },
      { path: '/media/ai-clip/material', name: '414', component: () => import('/@/views/business/media/ai-clip/material-list.vue'), meta: { title: '素材库' } },
      { path: '/media/ai-clip/template', name: '415', component: () => import('/@/views/business/media/ai-clip/template-list.vue'), meta: { title: '模板中心' } },
      { path: '/media/ai-clip/template/:id', name: '418', component: () => import('/@/views/business/media/ai-clip/template-detail.vue'), meta: { title: '模板详情', hideInMenu: true } },
      { path: '/media/ai-clip/script', name: '416', component: () => import('/@/views/business/media/ai-clip/script-list.vue'), meta: { title: '脚本工坊' } },
      { path: '/media/ai-clip/task', name: '417', component: () => import('/@/views/business/media/ai-clip/task-list.vue'), meta: { title: '成片任务' } },
      { path: '/media/ai-clip/export', name: '419', component: () => import('/@/views/business/media/ai-clip/export-list.vue'), meta: { title: '导出发布' } },
      { path: '/media/music/discover', name: '421', component: () => import('/@/views/business/media/music/discover.vue'), meta: { title: '发现音乐' } },
      { path: '/media/music/playlist', name: '422', component: () => import('/@/views/business/media/music/playlist-list.vue'), meta: { title: '歌单' } },
      { path: '/media/music/playlist/:id', name: '440', component: () => import('/@/views/business/media/music/playlist-detail.vue'), meta: { title: '歌单详情', hideInMenu: true } },
      { path: '/media/music/artist', name: '423', component: () => import('/@/views/business/media/music/artist-list.vue'), meta: { title: '歌手' } },
      { path: '/media/music/artist/:id', name: '441', component: () => import('/@/views/business/media/music/artist-detail.vue'), meta: { title: '歌手详情', hideInMenu: true } },
      { path: '/media/music/album', name: '424', component: () => import('/@/views/business/media/music/album-list.vue'), meta: { title: '专辑' } },
      { path: '/media/music/album/:id', name: '442', component: () => import('/@/views/business/media/music/album-detail.vue'), meta: { title: '专辑详情', hideInMenu: true } },
      { path: '/media/music/rank', name: '425', component: () => import('/@/views/business/media/music/rank.vue'), meta: { title: '音乐排行榜' } },
      { path: '/media/music/radio', name: '426', component: () => import('/@/views/business/media/music/radio.vue'), meta: { title: '播客电台' } },
      { path: '/media/music/favorite', name: '427', component: () => import('/@/views/business/media/music/favorite.vue'), meta: { title: '我喜欢' } },
      { path: '/media/music/recent', name: '428', component: () => import('/@/views/business/media/music/recent.vue'), meta: { title: '最近播放' } },
      { path: '/media/music/search', name: '429', component: () => import('/@/views/business/media/music/search.vue'), meta: { title: '音乐搜索' } },
      { path: '/media/music/lyric', name: '443', component: () => import('/@/views/business/media/music/lyric.vue'), meta: { title: '歌词' } },
      { path: '/media/yingyue/home', name: '431', component: () => import('/@/views/business/media/yingyue/home.vue'), meta: { title: '影月首页' } },
      { path: '/media/yingyue/library', name: '432', component: () => import('/@/views/business/media/yingyue/library.vue'), meta: { title: '片库' } },
      { path: '/media/yingyue/channel', name: '433', component: () => import('/@/views/business/media/yingyue/channel.vue'), meta: { title: '频道' } },
      { path: '/media/yingyue/rank', name: '434', component: () => import('/@/views/business/media/yingyue/rank.vue'), meta: { title: '影月排行' } },
      { path: '/media/yingyue/favorite', name: '435', component: () => import('/@/views/business/media/yingyue/favorite.vue'), meta: { title: '我的片单' } },
      { path: '/media/yingyue/history', name: '436', component: () => import('/@/views/business/media/yingyue/history.vue'), meta: { title: '观看历史' } },
      { path: '/media/yingyue/search', name: '437', component: () => import('/@/views/business/media/yingyue/search.vue'), meta: { title: '影月搜索' } },
      { path: '/media/yingyue/detail/:id', name: '438', component: () => import('/@/views/business/media/yingyue/detail.vue'), meta: { title: '影片详情', hideInMenu: true } },
      { path: '/media/yingyue/play/:id', name: '439', component: () => import('/@/views/business/media/yingyue/play.vue'), meta: { title: '播放', hideInMenu: true } },
      { path: '/customer/ticket', name: '501', component: () => import('/@/views/business/customer/ticket/ticket-list.vue'), meta: { title: '工单管理', icon: 'FileProtectOutlined' } },
      { path: '/customer/knowledge', name: '502', component: () => import('/@/views/business/customer/knowledge/knowledge-list.vue'), meta: { title: '知识库', icon: 'BookOutlined' } },
      { path: '/customer/info-query', name: '503', component: () => import('/@/views/business/customer/info-query/info-query.vue'), meta: { title: '信息查询', icon: 'SearchOutlined' } },
      { path: '/customer/qa', name: '512', component: () => import('/@/views/business/customer/qa/qa-list.vue'), meta: { title: '问答管理', icon: 'QuestionCircleOutlined' } },
      { path: '/mall-admin/activity', name: '601', component: () => import('/@/views/business/mall/admin/activity-list.vue'), meta: { title: '秒杀活动' } },
      { path: '/mall-admin/order', name: '602', component: () => import('/@/views/business/mall/admin/order-list.vue'), meta: { title: '商城订单' } },
      { path: '/mall-admin/member', name: '603', component: () => import('/@/views/business/mall/admin/member-list.vue'), meta: { title: '会员' } },
      { path: '/mall-admin/commission', name: '604', component: () => import('/@/views/business/mall/admin/commission-list.vue'), meta: { title: '分销佣金' } },
    ],
  },
];
