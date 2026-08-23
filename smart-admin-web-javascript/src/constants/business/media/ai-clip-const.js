/*
 * AI漫剪（参照剪映 / 度加 / CapCut）
 */
export const AI_CLIP_PROJECT_STATUS_ENUM = {
  DRAFT: { value: 10, desc: '草稿' },
  RENDERING: { value: 20, desc: '成片中' },
  DONE: { value: 30, desc: '已成片' },
  FAILED: { value: 40, desc: '失败' },
};

export const AI_CLIP_MATERIAL_TYPE_ENUM = {
  VIDEO: { value: 1, desc: '视频' },
  IMAGE: { value: 2, desc: '图片' },
  AUDIO: { value: 3, desc: '音频' },
  FONT: { value: 4, desc: '字体字幕' },
};

export const AI_CLIP_TEMPLATE_SCENE_ENUM = {
  GOODS: { value: 1, desc: '带货口播' },
  TUTORIAL: { value: 2, desc: '教程知识' },
  VLOG: { value: 3, desc: '生活Vlog' },
  ANIME: { value: 4, desc: '二次元漫剪' },
  NEWS: { value: 5, desc: '资讯快剪' },
};

export const AI_CLIP_TASK_STATUS_ENUM = {
  WAIT: { value: 10, desc: '排队中' },
  RUNNING: { value: 20, desc: '生成中' },
  SUCCESS: { value: 30, desc: '已完成' },
  FAILED: { value: 40, desc: '失败' },
};

export default {
  AI_CLIP_PROJECT_STATUS_ENUM,
  AI_CLIP_MATERIAL_TYPE_ENUM,
  AI_CLIP_TEMPLATE_SCENE_ENUM,
  AI_CLIP_TASK_STATUS_ENUM,
};
