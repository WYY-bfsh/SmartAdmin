/*
 * 影月播放器
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const yingyueApi = {
  home: () => getRequest('/media/yingyue/home'),
  queryLibrary: (param) => postRequest('/media/yingyue/library/query', param),
  channel: (category) => getRequest('/media/yingyue/channel', { category }),
  rank: () => getRequest('/media/yingyue/rank'),
  detail: (id) => getRequest(`/media/yingyue/detail/${id}`),
  play: (id, episodeNo) => getRequest(`/media/yingyue/play/${id}`, { episodeNo }),
  search: (keyword) => getRequest('/media/yingyue/search', { keyword }),
  favorite: () => getRequest('/media/yingyue/favorite'),
  history: () => getRequest('/media/yingyue/history'),
  toggleFavorite: (videoId) => postRequest('/media/yingyue/favorite/toggle', { videoId }),
  reportProgress: (param) => postRequest('/media/yingyue/history/report', param),
  streamUrl: (videoId) => {
    const base = String(import.meta.env.VITE_APP_API_URL || '').replace(/\/$/, '');
    return `${base}/media/yingyue/stream/${videoId}`;
  },
};
