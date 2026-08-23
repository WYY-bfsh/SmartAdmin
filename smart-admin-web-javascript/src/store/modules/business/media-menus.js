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

export function buildMediaCatalog() {
  return catalog(400, 0, '媒体中心', '/media', 'AppstoreOutlined', [
    catalog(410, 400, 'AI漫剪', '/media/ai-clip', 'ScissorOutlined', [
      page(411, 410, '工作台', '/media/ai-clip/workspace', '/business/media/ai-clip/workspace.vue', 'DashboardOutlined'),
      page(412, 410, '我的作品', '/media/ai-clip/project', '/business/media/ai-clip/project-list.vue', 'FolderOutlined', true),
      page(414, 410, '素材库', '/media/ai-clip/material', '/business/media/ai-clip/material-list.vue', 'PictureOutlined'),
      page(415, 410, '模板中心', '/media/ai-clip/template', '/business/media/ai-clip/template-list.vue', 'AppstoreOutlined'),
      page(416, 410, '脚本工坊', '/media/ai-clip/script', '/business/media/ai-clip/script-list.vue', 'EditOutlined'),
      page(417, 410, '成片任务', '/media/ai-clip/task', '/business/media/ai-clip/task-list.vue', 'ThunderboltOutlined'),
      page(419, 410, '导出发布', '/media/ai-clip/export', '/business/media/ai-clip/export-list.vue', 'CloudUploadOutlined'),
    ]),
    catalog(420, 400, '音乐播放器', '/media/music', 'CustomerServiceOutlined', [
      page(421, 420, '发现音乐', '/media/music/discover', '/business/media/music/discover.vue', 'FireOutlined'),
      page(422, 420, '歌单', '/media/music/playlist', '/business/media/music/playlist-list.vue', 'UnorderedListOutlined'),
      page(423, 420, '歌手', '/media/music/artist', '/business/media/music/artist-list.vue', 'UserOutlined'),
      page(424, 420, '专辑', '/media/music/album', '/business/media/music/album-list.vue', 'AppstoreOutlined'),
      page(425, 420, '排行榜', '/media/music/rank', '/business/media/music/rank.vue', 'TrophyOutlined'),
      page(426, 420, '播客电台', '/media/music/radio', '/business/media/music/radio.vue', 'SoundOutlined'),
      page(427, 420, '我喜欢', '/media/music/favorite', '/business/media/music/favorite.vue', 'HeartOutlined'),
      page(428, 420, '最近播放', '/media/music/recent', '/business/media/music/recent.vue', 'HistoryOutlined'),
      page(429, 420, '搜索', '/media/music/search', '/business/media/music/search.vue', 'SearchOutlined'),
      page(443, 420, '歌词', '/media/music/lyric', '/business/media/music/lyric.vue', 'SoundOutlined'),
    ]),
    catalog(430, 400, '影月播放器', '/media/yingyue', 'PlayCircleOutlined', [
      page(431, 430, '影月首页', '/media/yingyue/home', '/business/media/yingyue/home.vue', 'HomeOutlined'),
      page(432, 430, '片库', '/media/yingyue/library', '/business/media/yingyue/library.vue', 'AppstoreOutlined'),
      page(433, 430, '频道', '/media/yingyue/channel', '/business/media/yingyue/channel.vue', 'VideoCameraOutlined'),
      page(434, 430, '排行榜', '/media/yingyue/rank', '/business/media/yingyue/rank.vue', 'TrophyOutlined'),
      page(435, 430, '我的片单', '/media/yingyue/favorite', '/business/media/yingyue/favorite.vue', 'StarOutlined'),
      page(436, 430, '观看历史', '/media/yingyue/history', '/business/media/yingyue/history.vue', 'HistoryOutlined'),
      page(437, 430, '搜索', '/media/yingyue/search', '/business/media/yingyue/search.vue', 'SearchOutlined'),
    ]),
  ]);
}

function flattenMediaMenus() {
  return [
    { menuId: 400, menuName: '媒体中心', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 0, sort: 3, path: '/media', icon: 'AppstoreOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 410, menuName: 'AI漫剪', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 400, sort: 1, path: '/media/ai-clip', icon: 'ScissorOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 411, menuName: '工作台', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 1, path: '/media/ai-clip/workspace', component: '/business/media/ai-clip/workspace.vue', icon: 'DashboardOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 412, menuName: '我的作品', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 2, path: '/media/ai-clip/project', component: '/business/media/ai-clip/project-list.vue', icon: 'FolderOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false, cacheFlag: true },
    { menuId: 413, menuName: '作品剪辑', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 3, path: '/media/ai-clip/project/editor/:id', component: '/business/media/ai-clip/project-editor.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    { menuId: 414, menuName: '素材库', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 4, path: '/media/ai-clip/material', component: '/business/media/ai-clip/material-list.vue', icon: 'PictureOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 415, menuName: '模板中心', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 5, path: '/media/ai-clip/template', component: '/business/media/ai-clip/template-list.vue', icon: 'AppstoreOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 418, menuName: '模板详情', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 6, path: '/media/ai-clip/template/:id', component: '/business/media/ai-clip/template-detail.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    { menuId: 416, menuName: '脚本工坊', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 7, path: '/media/ai-clip/script', component: '/business/media/ai-clip/script-list.vue', icon: 'EditOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 417, menuName: '成片任务', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 8, path: '/media/ai-clip/task', component: '/business/media/ai-clip/task-list.vue', icon: 'ThunderboltOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 419, menuName: '导出发布', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 410, sort: 9, path: '/media/ai-clip/export', component: '/business/media/ai-clip/export-list.vue', icon: 'CloudUploadOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 420, menuName: '音乐播放器', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 400, sort: 2, path: '/media/music', icon: 'CustomerServiceOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 421, menuName: '发现音乐', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 1, path: '/media/music/discover', component: '/business/media/music/discover.vue', icon: 'FireOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 422, menuName: '歌单', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 2, path: '/media/music/playlist', component: '/business/media/music/playlist-list.vue', icon: 'UnorderedListOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 440, menuName: '歌单详情', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 3, path: '/media/music/playlist/:id', component: '/business/media/music/playlist-detail.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    { menuId: 423, menuName: '歌手', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 4, path: '/media/music/artist', component: '/business/media/music/artist-list.vue', icon: 'UserOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 441, menuName: '歌手详情', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 5, path: '/media/music/artist/:id', component: '/business/media/music/artist-detail.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    { menuId: 424, menuName: '专辑', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 6, path: '/media/music/album', component: '/business/media/music/album-list.vue', icon: 'AppstoreOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 442, menuName: '专辑详情', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 7, path: '/media/music/album/:id', component: '/business/media/music/album-detail.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    { menuId: 425, menuName: '排行榜', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 8, path: '/media/music/rank', component: '/business/media/music/rank.vue', icon: 'TrophyOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 426, menuName: '播客电台', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 9, path: '/media/music/radio', component: '/business/media/music/radio.vue', icon: 'SoundOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 427, menuName: '我喜欢', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 10, path: '/media/music/favorite', component: '/business/media/music/favorite.vue', icon: 'HeartOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 428, menuName: '最近播放', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 11, path: '/media/music/recent', component: '/business/media/music/recent.vue', icon: 'HistoryOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 429, menuName: '搜索', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 12, path: '/media/music/search', component: '/business/media/music/search.vue', icon: 'SearchOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 443, menuName: '歌词', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 420, sort: 13, path: '/media/music/lyric', component: '/business/media/music/lyric.vue', icon: 'SoundOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 430, menuName: '影月播放器', menuType: MENU_TYPE_ENUM.CATALOG.value, parentId: 400, sort: 3, path: '/media/yingyue', icon: 'PlayCircleOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 431, menuName: '影月首页', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 1, path: '/media/yingyue/home', component: '/business/media/yingyue/home.vue', icon: 'HomeOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 432, menuName: '片库', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 2, path: '/media/yingyue/library', component: '/business/media/yingyue/library.vue', icon: 'AppstoreOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 433, menuName: '频道', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 3, path: '/media/yingyue/channel', component: '/business/media/yingyue/channel.vue', icon: 'VideoCameraOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 434, menuName: '排行榜', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 4, path: '/media/yingyue/rank', component: '/business/media/yingyue/rank.vue', icon: 'TrophyOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 435, menuName: '我的片单', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 5, path: '/media/yingyue/favorite', component: '/business/media/yingyue/favorite.vue', icon: 'StarOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 436, menuName: '观看历史', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 6, path: '/media/yingyue/history', component: '/business/media/yingyue/history.vue', icon: 'HistoryOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 437, menuName: '搜索', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 7, path: '/media/yingyue/search', component: '/business/media/yingyue/search.vue', icon: 'SearchOutlined', visibleFlag: true, disabledFlag: false, deletedFlag: false },
    { menuId: 438, menuName: '影片详情', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 8, path: '/media/yingyue/detail/:id', component: '/business/media/yingyue/detail.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    { menuId: 439, menuName: '播放', menuType: MENU_TYPE_ENUM.MENU.value, parentId: 430, sort: 9, path: '/media/yingyue/play/:id', component: '/business/media/yingyue/play.vue', visibleFlag: false, disabledFlag: false, deletedFlag: false },
    point(450, 411, '查看工作台', 'media:aiclip:query'),
    point(451, 412, '管理作品', 'media:aiclip:project'),
    point(452, 414, '管理素材', 'media:aiclip:material'),
    point(453, 417, '提交成片', 'media:aiclip:task'),
    point(454, 421, '播放音乐', 'media:music:play'),
    point(455, 431, '播放影片', 'media:yingyue:play'),
  ];
}

export function prependMediaMenuTree(menuTree) {
  const tree = Array.isArray(menuTree) ? menuTree : [];
  if (tree.some((e) => e.menuId === 400 || e.menuName === '媒体中心')) {
    return tree;
  }
  return [buildMediaCatalog(), ...tree];
}

export function ensureMediaMenus(menuList) {
  const list = Array.isArray(menuList) ? [...menuList] : [];
  const exists = list.some((e) => e.menuId === 400 || e.path === '/media/ai-clip/workspace');
  if (exists) {
    return list;
  }
  return list.concat(flattenMediaMenus());
}
