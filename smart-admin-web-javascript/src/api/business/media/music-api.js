/*
 * 音乐播放器
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const musicApi = {
  discover: () => getRequest('/media/music/discover'),
  querySongs: () => getRequest('/media/music/songs'),
  reportPlay: (songId) => postRequest('/media/music/play/report', { songId }),
  queryPlaylist: (param) => postRequest('/media/music/playlist/query', param),
  playlistDetail: (id) => getRequest(`/media/music/playlist/detail/${id}`),
  queryArtist: (param) => postRequest('/media/music/artist/query', param),
  artistDetail: (id) => getRequest(`/media/music/artist/detail/${id}`),
  queryAlbum: (param) => postRequest('/media/music/album/query', param),
  albumDetail: (id) => getRequest(`/media/music/album/detail/${id}`),
  rank: (type) => getRequest(`/media/music/rank/${type}`),
  radio: () => getRequest('/media/music/radio'),
  search: (keyword) => getRequest('/media/music/search', { keyword }),
  favorite: () => getRequest('/media/music/favorite'),
  recent: () => getRequest('/media/music/recent'),
  toggleLike: (songId) => postRequest('/media/music/like', { songId }),
  lyric: (songId) => getRequest(`/media/music/lyric/${songId}`),
  streamUrl: (songId) => {
    const base = String(import.meta.env.VITE_APP_API_URL || '').replace(/\/$/, '');
    return `${base}/media/music/stream/${songId}`;
  },
};
