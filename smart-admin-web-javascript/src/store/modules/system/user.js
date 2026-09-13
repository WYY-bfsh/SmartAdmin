/*
 * 登录用户
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-06 20:55:09
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import _ from 'lodash';
import { defineStore } from 'pinia';
import localKey from '/@/constants/local-storage-key-const';
import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
import { messageApi } from '/@/api/support/message-api';
import { smartSentry } from '/@/lib/smart-sentry';
import { localRead, localSave, localRemove } from '/@/utils/local-util';
import { ensureMediaMenus, prependMediaMenuTree } from '/@/store/modules/business/media-menus';
import { ensureCustomerMenus, prependCustomerMenuTree } from '/@/store/modules/business/customer-menus';
import { ensureMallMenus, prependMallMenuTree } from '/@/store/modules/business/mall-menus';


export const useUserStore = defineStore({
  id: 'userStore',
  state: () => ({
    token: '',
    //员工id
    employeeId: '',
    // 头像
    avatar: '',
    //登录名
    loginName: '',
    //姓名
    actualName: '',
    //手机号
    phone: '',
    //部门id
    departmentId: '',
    //部门名词
    departmentName: '',
    //是否需要修改密码
    needUpdatePwdFlag: false,
    //是否为超级管理员
    administratorFlag: true,
    //上次登录ip
    lastLoginIp: '',
    //上次登录ip地区
    lastLoginIpRegion: '',
    //上次登录 设备
    lastLoginUserAgent: '',
    //上次登录时间
    lastLoginTime: '',
    //左侧菜单树形结构
    menuTree: [],
    //存在页面路由的菜单集合
    menuRouterList: [],
    //是否完成menuRouter初始化
    menuRouterInitFlag: false,
    //父类菜单集合
    menuParentIdListMap: new Map(),
    // 功能点集合
    pointsList: [],
    // 标签页
    tagNav: null,
    // 缓存
    keepAliveIncludes: [],
    // 未读消息数量
    unreadMessageCount: 0,
    // 待办工作数
    toBeDoneCount: 0,
  }),
  getters: {
    getToken(state) {
      if (state.token) {
        return state.token;
      }
      return localRead(localKey.USER_TOKEN);
    },
    getNeedUpdatePwdFlag(state){
      return state.needUpdatePwdFlag;
    },
    //是否初始化了 路由
    getMenuRouterInitFlag(state) {
      return state.menuRouterInitFlag;
    },
    //菜单树
    getMenuTree(state) {
      return prependMallMenuTree(prependCustomerMenuTree(prependMediaMenuTree(prependPayMenuTree(state.menuTree))));
    },
    //菜单的路由
    getMenuRouterList(state) {
      return state.menuRouterList;
    },
    //菜单的父级id
    getMenuParentIdListMap(state) {
      return state.menuParentIdListMap;
    },
    //功能点
    getPointList(state) {
      if (_.isEmpty(state.pointsList)) {
        let localUserPoints = localRead(localKey.USER_POINTS) || '';
        state.pointsList = localUserPoints ? JSON.parse(localUserPoints) : [];
      }
      return state.pointsList;
    },
    //标签页
    getTagNav(state) {
      if (_.isNull(state.tagNav)) {
        let localTagNav = localRead(localKey.USER_TAG_NAV) || '';
        state.tagNav = localTagNav ? JSON.parse(localTagNav) : [];
      }
      let tagNavList = _.cloneDeep(state.tagNav) || [];
      tagNavList.unshift({
        menuName: HOME_PAGE_NAME,
        menuTitle: '首页',
      });
      return tagNavList;
    },
  },

  actions: {
    logout() {
      this.token = '';
      this.menuList = [];
      this.tagNav = [];
      this.unreadMessageCount = 0;
      localRemove(localKey.USER_TOKEN);
      localRemove(localKey.USER_POINTS);
      localRemove(localKey.USER_TAG_NAV);
      localRemove(localKey.APP_CONFIG);
      localRemove(localKey.HOME_QUICK_ENTRY);
      localRemove(localKey.NOTICE_READ);
      localRemove(localKey.TO_BE_DONE);
    },
    // 查询未读消息数量
    async queryUnreadMessageCount() {
      try {
        let result = await messageApi.queryUnreadCount();
        this.unreadMessageCount = result.data;
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
    async queryToBeDoneList() {
      try {
        let localToBeDoneList = localRead(localKey.TO_BE_DONE);
        if (localToBeDoneList) {
          this.toBeDoneCount = JSON.parse(localToBeDoneList).filter((e) => !e.doneFlag).length;
        }
      } catch (err) {
        smartSentry.captureError(err);
      }
    },
    //设置登录信息
    setUserLoginInfo(data) {
      // 用户基本信息
      this.token = data.token;
      this.employeeId = data.employeeId;
      this.avatar = data.avatar;
      this.loginName = data.loginName;
      this.actualName = data.actualName;
      this.phone = data.phone;
      this.departmentId = data.departmentId;
      this.departmentName = data.departmentName;
      this.needUpdatePwdFlag = data.needUpdatePwdFlag;
      this.administratorFlag = data.administratorFlag;
      this.lastLoginIp = data.lastLoginIp;
      this.lastLoginIpRegion = data.lastLoginIpRegion;
      this.lastLoginUserAgent = data.lastLoginUserAgent;
      this.lastLoginTime = data.lastLoginTime;

      //菜单权限（库里还没有支付菜单时，前端补一套，保证侧栏能看到）
      data.menuList = ensureMallMenus(ensureCustomerMenus(ensureMediaMenus(ensurePayMenus(data.menuList))));

      //菜单权限
      this.menuTree = buildMenuTree(data.menuList);

      //拥有路由的菜单
      this.menuRouterList = data.menuList.filter((e) => e.path || e.frameUrl);

      //父级菜单集合
      this.menuParentIdListMap = buildMenuParentIdListMap(this.menuTree);

      //功能点
      this.pointsList = data.menuList.filter((menu) => menu.menuType === MENU_TYPE_ENUM.POINTS.value && menu.visibleFlag && !menu.disabledFlag);

      // 获取用户未读消息
      this.queryUnreadMessageCount();
      // 获取待办工作数
      this.queryToBeDoneList();
    },

    setToken(token) {
      this.token = token;
    },

    //设置标签页
    setTagNav(route, from) {
      if (_.isNull(this.tagNav)) {
        let localTagNav = localRead(localKey.USER_TAG_NAV) || '';
        this.tagNav = localTagNav ? JSON.parse(localTagNav) : [];
      }
      // name唯一标识
      let name = route.name;
      if (!name || name === HOME_PAGE_NAME || name === '403' || name === '404') {
        return;
      }
      let findTag = (this.tagNav || []).find((e) => e.menuName === name);
      if (findTag) {
        // @ts-ignore
        findTag.fromMenuName = from.name;
        findTag.fromMenuQuery = from.query;
        findTag.menuQuery = route.query;
      } else {
        // @ts-ignore
        this.tagNav.push({
          // @ts-ignore
          menuName: name,
          // @ts-ignore
          menuTitle: route.meta.title,
          menuQuery: route.query,
          menuIcon:route.meta.icon,
          // @ts-ignore
          fromMenuName: from.name,
          fromMenuQuery: from.query,
        });
      }
      localSave(localKey.USER_TAG_NAV, JSON.stringify(this.tagNav));
    },
    //关闭标签页
    closeTagNav(menuName, closeAll) {
      if (_.isEmpty(this.getTagNav)) return;
      if (closeAll && !menuName) {
        this.tagNav = [];
        this.clearKeepAliveIncludes();
      } else {
        let findIndex = (this.tagNav || []).findIndex((e) => e.menuName === menuName);
        if (closeAll) {
          if (findIndex === -1) {
            this.tagNav = [];
            this.clearKeepAliveIncludes();
          } else {
            let tagNavElement = (this.tagNav || [])[findIndex];
            this.tagNav = [tagNavElement];
            this.clearKeepAliveIncludes(tagNavElement.menuName);
          }
        } else {
          (this.tagNav || []).splice(findIndex, 1);
          this.deleteKeepAliveIncludes(menuName);
        }
      }
      localSave(localKey.USER_TAG_NAV, JSON.stringify(this.tagNav));
    },
    //关闭页面
    closePage(route, router, path) {
      if (!this.getTagNav || _.isEmpty(this.getTagNav)) return;
      if (path) {
        router.push({ path });
      } else {
        // 寻找tagNav
        let index = this.getTagNav.findIndex((e) => e.menuName === route.name);
        if (index === -1) {
          router.push({ name: HOME_PAGE_NAME });
        } else {
          let tagNav = this.getTagNav[index];
          if (tagNav.fromMenuName && this.getTagNav.some((e) => e.menuName === tagNav.fromMenuName)) {
            router.push({ name: tagNav.fromMenuName, query: tagNav.fromMenuQuery });
          } else {
            // 查询左侧tag
            let leftTagNav = this.getTagNav[index - 1];
            router.push({ name: leftTagNav.menuName, query: leftTagNav.menuQuery });
          }
        }
      }
      this.closeTagNav(route.name, false);
    },
    // 加入缓存
    pushKeepAliveIncludes(val) {
      if (!val) {
        return;
      }
      if (!this.keepAliveIncludes) {
        this.keepAliveIncludes = [];
      }
      if (this.keepAliveIncludes.length < 30) {
        let number = this.keepAliveIncludes.findIndex((e) => e === val);
        if (number === -1) {
          this.keepAliveIncludes.push(val);
        }
      }
    },
    // 删除缓存
    deleteKeepAliveIncludes(val) {
      if (!this.keepAliveIncludes || !val) {
        return;
      }
      let number = this.keepAliveIncludes.findIndex((e) => e === val);
      if (number !== -1) {
        this.keepAliveIncludes.splice(number, 1);
      }
    },
    // 清空缓存
    clearKeepAliveIncludes(val) {
      if (!val || !this.keepAliveIncludes.includes(val)) {
        this.keepAliveIncludes = [];
        return;
      }
      this.keepAliveIncludes = [val];
    },
  },
});

/**
 * 构建菜单父级集合
 */
function buildMenuParentIdListMap(menuTree) {
  let menuParentIdListMap = new Map();
  recursiveBuildMenuParentIdListMap(menuTree, [], menuParentIdListMap);
  return menuParentIdListMap;
}

function recursiveBuildMenuParentIdListMap(menuList, parentMenuList, menuParentIdListMap) {
  for (const e of menuList) {
    // 顶级parentMenuList清空
    if (e.parentId === 0) {
      parentMenuList = [];
    }
    let menuIdStr = e.menuId.toString();
    let cloneParentMenuList = _.cloneDeep(parentMenuList);
    if (!_.isEmpty(e.children) && e.menuName) {
      // 递归
      cloneParentMenuList.push({ name: menuIdStr, title: e.menuName });
      recursiveBuildMenuParentIdListMap(e.children, cloneParentMenuList, menuParentIdListMap);
    } else {
      menuParentIdListMap.set(menuIdStr, cloneParentMenuList);
    }
  }
}

function buildPayCatalog() {
  return {
    menuId: 300,
    menuName: '微信支付',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: 0,
    path: '/pay',
    icon: 'WechatOutlined',
    visibleFlag: true,
    disabledFlag: false,
    deletedFlag: false,
    children: [
      {
        menuId: 301,
        menuName: '支付订单',
        menuType: MENU_TYPE_ENUM.MENU.value,
        parentId: 300,
        path: '/pay/order',
        component: '/business/pay/pay-order-list.vue',
        icon: 'AccountBookOutlined',
        visibleFlag: true,
        disabledFlag: false,
        deletedFlag: false,
        cacheFlag: true,
        frameFlag: false,
      },
      {
        menuId: 302,
        menuName: '商户配置',
        menuType: MENU_TYPE_ENUM.MENU.value,
        parentId: 300,
        path: '/pay/config',
        component: '/business/pay/wechat-pay-config.vue',
        icon: 'SettingOutlined',
        visibleFlag: true,
        disabledFlag: false,
        deletedFlag: false,
        cacheFlag: false,
        frameFlag: false,
      },
      {
        menuId: 313,
        menuName: '支付宝配置',
        menuType: MENU_TYPE_ENUM.MENU.value,
        parentId: 300,
        path: '/pay/alipay-config',
        component: '/business/pay/alipay-pay-config.vue',
        icon: 'AlipayCircleOutlined',
        visibleFlag: true,
        disabledFlag: false,
        deletedFlag: false,
        cacheFlag: false,
        frameFlag: false,
      },
      {
        menuId: 314,
        menuName: '支付对账',
        menuType: MENU_TYPE_ENUM.MENU.value,
        parentId: 300,
        path: '/pay/recon',
        component: '/business/pay/pay-recon-list.vue',
        icon: 'AuditOutlined',
        visibleFlag: true,
        disabledFlag: false,
        deletedFlag: false,
        cacheFlag: true,
        frameFlag: false,
      },
    ],
  };
}

function prependPayMenuTree(menuTree) {
  const tree = Array.isArray(menuTree) ? menuTree : [];
  if (tree.some((e) => e.menuId === 300 || e.menuName === '微信支付')) {
    return tree;
  }
  return [buildPayCatalog(), ...tree];
}

export { ensureMediaMenus } from '/@/store/modules/business/media-menus';
export { ensureCustomerMenus } from '/@/store/modules/business/customer-menus';
export { ensureMallMenus } from '/@/store/modules/business/mall-menus';

/**
 * 后端未写入支付菜单时，前端补齐目录/页面/按钮权限
 */
export function ensurePayMenus(menuList) {
  let list = Array.isArray(menuList) ? [...menuList] : [];
  const exists = list.some(
    (e) => e.menuId === 300 || e.menuId === 301 || e.menuId === 310 || e.component === '/business/pay/pay-order-list.vue'
  );
  if (!exists) {
    list = list.concat([
      { menuId: 300, menuName: '微信支付', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 0, sort: 4, path: '/pay', component: null, icon: 'WechatOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: false, frameFlag: false },
      { menuId: 301, menuName: '支付订单', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 300, sort: 1, path: '/pay/order', component: '/business/pay/pay-order-list.vue', icon: 'AccountBookOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: true, frameFlag: false },
      { menuId: 302, menuName: '商户配置', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 300, sort: 2, path: '/pay/config', component: '/business/pay/wechat-pay-config.vue', icon: 'SettingOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: false, frameFlag: false },
      { menuId: 303, menuName: '查询订单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 301, webPerms: 'pay:order:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 304, menuName: '发起支付', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 301, webPerms: 'pay:order:create', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 305, menuName: '关闭订单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 301, webPerms: 'pay:order:close', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 306, menuName: '申请退款', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 301, webPerms: 'pay:order:refund', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 307, menuName: '同步状态', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 301, webPerms: 'pay:order:sync', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 308, menuName: '查看配置', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 302, webPerms: 'pay:config:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    ]);
  }
  const parent = list.find((e) => e.menuId === 310 || e.menuId === 300 || e.menuName === '微信支付');
  const parentId = parent ? parent.menuId : 300;
  if (!list.some((e) => e.component === '/business/pay/alipay-pay-config.vue' || e.menuId === 313)) {
    list.push({
      menuId: 313,
      menuName: '支付宝配置',
      menuType: MENU_TYPE_ENUM.MENU.value,
      parentId,
      sort: 3,
      path: '/pay/alipay-config',
      component: '/business/pay/alipay-pay-config.vue',
      icon: 'AlipayCircleOutlined',
      visibleFlag: true,
      disabledFlag: false,
      deletedFlag: false,
      cacheFlag: false,
      frameFlag: false,
    });
  }
  if (!list.some((e) => e.component === '/business/pay/pay-recon-list.vue' || e.menuId === 314)) {
    list.push(
      {
        menuId: 314,
        menuName: '支付对账',
        menuType: MENU_TYPE_ENUM.MENU.value,
        parentId,
        sort: 4,
        path: '/pay/recon',
        component: '/business/pay/pay-recon-list.vue',
        icon: 'AuditOutlined',
        visibleFlag: true,
        disabledFlag: false,
        deletedFlag: false,
        cacheFlag: true,
        frameFlag: false,
      },
      { menuId: 315, menuName: '查询对账', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 316, menuName: '拉取账单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:pull', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 317, menuName: '上传账单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:upload', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 318, menuName: '演示对账', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:mock', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 319, menuName: '核销差异', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:handle', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 320, menuName: '导出明细', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:export', visibleFlag: true, disabledFlag: false, deletedFlag: false },
      { menuId: 321, menuName: '删除批次', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 314, webPerms: 'pay:recon:delete', visibleFlag: true, disabledFlag: false, deletedFlag: false }
    );
  } else {
    const points = [
      { menuId: 315, menuName: '查询对账', webPerms: 'pay:recon:query' },
      { menuId: 316, menuName: '拉取账单', webPerms: 'pay:recon:pull' },
      { menuId: 317, menuName: '上传账单', webPerms: 'pay:recon:upload' },
      { menuId: 318, menuName: '演示对账', webPerms: 'pay:recon:mock' },
      { menuId: 319, menuName: '核销差异', webPerms: 'pay:recon:handle' },
      { menuId: 320, menuName: '导出明细', webPerms: 'pay:recon:export' },
      { menuId: 321, menuName: '删除批次', webPerms: 'pay:recon:delete' },
    ];
    for (const point of points) {
      if (!list.some((e) => e.webPerms === point.webPerms || e.menuId === point.menuId)) {
        list.push({
          ...point,
          menuType: MENU_TYPE_ENUM.POINTS.value,
          parentId: 314,
          visibleFlag: true,
          disabledFlag: false,
          deletedFlag: false,
        });
      }
    }
  }
  return list;
}

/**
 * 构建菜单树
 *
 * @param  menuList
 * @returns
 */
function buildMenuTree(menuList) {
  //1 获取所有 有效的 目录和菜单
  let catalogAndMenuList = menuList.filter((menu) => menu.menuType !== MENU_TYPE_ENUM.POINTS.value && menu.visibleFlag && !menu.disabledFlag);

  //2 获取顶级目录
  let topCatalogList = catalogAndMenuList.filter((menu) => menu.parentId === 0);
  for (const topCatalog of topCatalogList) {
    buildMenuChildren(topCatalog, catalogAndMenuList);
  }
  return topCatalogList;
}

function buildMenuChildren(menu, allMenuList) {
  let children = allMenuList.filter((e) => e.parentId === menu.menuId);
  if (children.length === 0) {
    return;
  }
  menu.children = children;
  for (const item of children) {
    buildMenuChildren(item, allMenuList);
  }
}
