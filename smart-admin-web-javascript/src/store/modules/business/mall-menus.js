import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';

function page(menuId, parentId, name, path, component, icon) {
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
    cacheFlag: false,
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

export function ensureMallMenus(menuList) {
  const list = Array.isArray(menuList) ? [...menuList] : [];
  const exists = list.some((e) => e.menuId === 600 || e.component === '/business/mall/admin/activity-list.vue');
  if (exists) {
    if (!list.some((e) => e.menuId === 617)) {
      list.push(point(617, 603, '维护会员', 'mall:member:save'));
    }
    if (!list.some((e) => e.menuId === 618)) {
      list.push(point(618, 602, '确认收款', 'mall:order:ship'));
    }
    return list;
  }
  return list.concat([
    { menuId: 600, menuName: '秒杀商城', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 0, sort: 3, path: '/mall-admin', icon: 'ShoppingOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: false, frameFlag: false },
    page(601, 600, '秒杀活动', '/mall-admin/activity', '/business/mall/admin/activity-list.vue', 'ThunderboltOutlined'),
    page(602, 600, '商城订单', '/mall-admin/order', '/business/mall/admin/order-list.vue', 'AccountBookOutlined'),
    page(603, 600, '会员', '/mall-admin/member', '/business/mall/admin/member-list.vue', 'TeamOutlined'),
    page(604, 600, '分销佣金', '/mall-admin/commission', '/business/mall/admin/commission-list.vue', 'PayCircleOutlined'),
    point(611, 601, '查询活动', 'mall:activity:query'),
    point(612, 601, '保存活动', 'mall:activity:save'),
    point(613, 602, '查询订单', 'mall:order:query'),
    point(614, 602, '发货', 'mall:order:ship'),
    point(615, 603, '查询会员', 'mall:member:query'),
    point(617, 603, '维护会员', 'mall:member:save'),
    point(616, 604, '查询佣金', 'mall:commission:query'),
  ]);
}

export function prependMallMenuTree(menuTree) {
  const tree = Array.isArray(menuTree) ? menuTree : [];
  if (tree.some((e) => e.menuId === 600 || e.menuName === '秒杀商城')) {
    return tree;
  }
  return [
    {
      menuId: 600,
      menuName: '秒杀商城',
      menuType: MENU_TYPE_ENUM.CATALOG.value,
      parentId: 0,
      path: '/mall-admin',
      icon: 'ShoppingOutlined',
      visibleFlag: true,
      disabledFlag: false,
      deletedFlag: false,
      children: [
        page(601, 600, '秒杀活动', '/mall-admin/activity', '/business/mall/admin/activity-list.vue', 'ThunderboltOutlined'),
        page(602, 600, '商城订单', '/mall-admin/order', '/business/mall/admin/order-list.vue', 'AccountBookOutlined'),
        page(603, 600, '会员', '/mall-admin/member', '/business/mall/admin/member-list.vue', 'TeamOutlined'),
        page(604, 600, '分销佣金', '/mall-admin/commission', '/business/mall/admin/commission-list.vue', 'PayCircleOutlined'),
      ],
    },
    ...tree,
  ];
}
