/*
 * 音乐播放器（参照网易云音乐 / QQ音乐）
 */
export const MUSIC_PLAY_MODE_ENUM = {
  SEQUENCE: { value: 1, desc: '顺序播放' },
  LOOP: { value: 2, desc: '单曲循环' },
  RANDOM: { value: 3, desc: '随机播放' },
};

export const MUSIC_QUALITY_ENUM = {
  STANDARD: { value: 128, desc: '标准 128K' },
  HIGH: { value: 320, desc: '高品 320K' },
  LOSSLESS: { value: 999, desc: '无损' },
};

export const MUSIC_RANK_TYPE_ENUM = {
  SOAR: { value: 1, desc: '飙升榜' },
  NEW: { value: 2, desc: '新歌榜' },
  HOT: { value: 3, desc: '热歌榜' },
  ORIGINAL: { value: 4, desc: '原创榜' },
};

export default {
  MUSIC_PLAY_MODE_ENUM,
  MUSIC_QUALITY_ENUM,
  MUSIC_RANK_TYPE_ENUM,
};
