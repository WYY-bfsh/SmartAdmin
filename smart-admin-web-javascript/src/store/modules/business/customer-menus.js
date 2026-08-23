/*
 * 客服中心 - 菜单注入
 *
 * 在后端库未写入菜单时，前端补齐菜单；同时支持侧边栏树形结构。
 */
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';

function page(menuId, parentId, name, path, component, icon, cacheFlag = false) {
  return {
    menuId,
    menuName: name,
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId,
    path,
    component,
    icon,
    visibleFlag: true,
    disabledFlag: false,
    deletedFlag: false,
    cacheFlag,
    frameFlag: false,
  };
}

function point(menuId, parentId, name, perms) {
  return {
    menuId,
    menuName: name,
    menuType: MENU_TYPE_ENUM.POINTS.value,
    parentId,
    webPerms: perms,
    visibleFlag: true,
    disabledFlag: false,
    deletedFlag: false,
  };
}

function catalog(menuId, parentId, name, path, icon, children) {
  return {
    menuId,
    menuName: name,
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId,
    path,
    icon,
    visibleFlag: true,
    disabledFlag: false,
    deletedFlag: false,
    children,
  };
}

function buildCustomerCatalog() {
  return catalog(500, 0, '客服中心', '/customer', 'CustomerServiceOutlined', [
    page(501, 500, '工单管理', '/customer/ticket', '/business/customer/ticket/ticket-list.vue', 'FileProtectOutlined', true),
    page(502, 500, '知识库', '/customer/knowledge', '/business/customer/knowledge/knowledge-list.vue', 'BookOutlined', true),
    page(503, 500, '信息查询', '/customer/info-query', '/business/customer/info-query/info-query.vue', 'SearchOutlined', false),
    page(512, 500, '问答管理', '/customer/qa', '/business/customer/qa/qa-list.vue', 'QuestionCircleOutlined', true),
  ]);
}

function flattenCustomerMenus() {
  return [
    { menuId: 500, menuName: '客服中心', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 0, sort: 5, path: '/customer', component: null, icon: 'CustomerServiceOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: false, frameFlag: false },
    { menuId: 501, menuName: '工单管理', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 500, sort: 1, path: '/customer/ticket', component: '/business/customer/ticket/ticket-list.vue', icon: 'FileProtectOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: true, frameFlag: false },
    { menuId: 502, menuName: '知识库', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 500, sort: 2, path: '/customer/knowledge', component: '/business/customer/knowledge/knowledge-list.vue', icon: 'BookOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: true, frameFlag: false },
    { menuId: 503, menuName: '信息查询', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 500, sort: 3, path: '/customer/info-query', component: '/business/customer/info-query/info-query.vue', icon: 'SearchOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: false, frameFlag: false },
    { menuId: 504, menuName: '查询工单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 501, webPerms: 'cs:ticket:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 505, menuName: '创建工单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 501, webPerms: 'cs:ticket:create', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 506, menuName: '回复工单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 501, webPerms: 'cs:ticket:reply', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 507, menuName: '关闭工单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 501, webPerms: 'cs:ticket:close', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 508, menuName: '删除工单', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 501, webPerms: 'cs:ticket:delete', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 509, menuName: '查询知识', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 502, webPerms: 'cs:knowledge:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 510, menuName: '编辑知识', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 502, webPerms: 'cs:knowledge:edit', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 511, menuName: '查询信息', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 503, webPerms: 'cs:info:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 512, menuName: '问答管理', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 500, sort: 4, path: '/customer/qa', component: '/business/customer/qa/qa-list.vue', icon: 'QuestionCircleOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: true, frameFlag: false },
    { menuId: 513, menuName: '查询问答', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 512, webPerms: 'cs:qa:query', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 514, menuName: '创建提问', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 512, webPerms: 'cs:qa:ask', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 515, menuName: '回答/驳回', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 512, webPerms: 'cs:qa:answer', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 516, menuName: '删除问答', menuType: MENU_TYPE_ENUM.POINTS.value, parentId: 512, webPerms: 'cs:qa:delete', visibleFlag: true, disabledFlag: false, deletedFlag: false },
  ];
}

export function ensureCustomerMenus(menuList) {
  const list = Array.isArray(menuList) ? [...menuList] : [];
  const exists = list.some(
    (e) => e.menuId === 500 || e.menuId === 501 || e.component === '/business/customer/ticket/ticket-list.vue'
  );
  if (exists) {
    return list;
  }
  return list.concat(flattenCustomerMenus());
}

export function prependCustomerMenuTree(menuTree) {
  const tree = Array.isArray(menuTree) ? menuTree : [];
  if (tree.some((e) => e.menuId === 500 || e.menuName === '客服中心')) {
    return tree;
  }
  return [buildCustomerCatalog(), ...tree];
}