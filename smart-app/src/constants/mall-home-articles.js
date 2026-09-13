export const MALL_HOME_ARTICLES = {
  company: {
    id: 'company',
    navTitle: '公告详情',
    title: '臻品荟企业介绍',
    summary: '专注高端唐卡创作、收藏与文化传播，古法传承、保真溯源。',
    time: '2026-05-11 21:18:27',
    cover: '/static/images/mall/company-store.jpg',
  },
  notice: {
    id: 'notice',
    navTitle: '公告详情',
    title: '合作才能发展，共赢才是王道',
    summary: '以匠心传文脉，以正品立口碑，让唐卡成为修身静心、典藏传家之选。',
    time: '2026-05-11 21:18:00',
    cover: '/static/images/mall/notice-plan.jpg',
  },
};

export function getMallHomeArticle(id) {
  return MALL_HOME_ARTICLES[id] || null;
}
