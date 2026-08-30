export const MALL_HOME_ARTICLES = {
  company: {
    id: 'company',
    navTitle: '公告详情',
    title: '臻品荟企业介绍',
    time: '2026-05-11 21:18:27',
    cover: '/static/images/mall/company-store.jpg',
  },
  notice: {
    id: 'notice',
    navTitle: '公告详情',
    title: '合作才能发展，共赢才是王道',
    time: '2026-05-11 21:18:00',
    cover: '/static/images/mall/notice-plan.jpg',
  },
};

export function getMallHomeArticle(id) {
  return MALL_HOME_ARTICLES[id] || null;
}
