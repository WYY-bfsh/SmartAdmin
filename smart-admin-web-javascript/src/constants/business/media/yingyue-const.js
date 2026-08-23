/*
 * 影月播放器（参照爱奇艺 / 腾讯视频 / 哔哩哔哩）
 */
export const YINGYUE_CATEGORY_ENUM = {
  MOVIE: { value: 1, desc: '电影' },
  SERIES: { value: 2, desc: '电视剧' },
  VARIETY: { value: 3, desc: '综艺' },
  ANIME: { value: 4, desc: '动漫' },
  DOC: { value: 5, desc: '纪录片' },
};

export const YINGYUE_AREA_ENUM = {
  CN: { value: 'cn', desc: '华语' },
  US: { value: 'us', desc: '欧美' },
  KR: { value: 'kr', desc: '韩国' },
  JP: { value: 'jp', desc: '日本' },
  OTHER: { value: 'other', desc: '其他' },
};

export const YINGYUE_QUALITY_ENUM = {
  SD: { value: '480p', desc: '标清' },
  HD: { value: '720p', desc: '高清' },
  FHD: { value: '1080p', desc: '超清 1080P' },
  UHD: { value: '4k', desc: '蓝光 4K' },
};

export default {
  YINGYUE_CATEGORY_ENUM,
  YINGYUE_AREA_ENUM,
  YINGYUE_QUALITY_ENUM,
};
