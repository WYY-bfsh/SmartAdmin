/**
 * 秒杀场次时间与预览窗口。
 * 开售前 PREVIEW_MINUTES 分钟可进商品页预览，但不能下单（以后端 saleStatus 为准）。
 */
const PREVIEW_MINUTES = 30;
const PREVIEW_MS = PREVIEW_MINUTES * 60 * 1000;

export function parseMallTime(value) {
  if (!value) {
    return 0;
  }
  if (typeof value === 'number') {
    return value;
  }
  const raw = String(value).trim();
  const normalized = raw.includes('T') ? raw : raw.replace(/-/g, '/');
  const ts = new Date(normalized).getTime();
  return Number.isNaN(ts) ? 0 : ts;
}

export function pad2(n) {
  return String(n).padStart(2, '0');
}

export function formatRemain(ms) {
  const safe = Math.max(0, ms);
  const total = Math.floor(safe / 1000);
  const d = Math.floor(total / 86400);
  const h = Math.floor((total % 86400) / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  if (d > 0) {
    return `${d}天 ${pad2(h)}:${pad2(m)}:${pad2(s)}`;
  }
  return `${pad2(h)}:${pad2(m)}:${pad2(s)}`;
}

export function formatClock(ts) {
  if (!ts) {
    return '--:--';
  }
  const d = new Date(ts);
  const month = d.getMonth() + 1;
  const day = d.getDate();
  const hm = `${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
  const now = new Date();
  if (d.getFullYear() === now.getFullYear() && month === now.getMonth() + 1 && day === now.getDate()) {
    return hm;
  }
  return `${month}月${day}日 ${hm}`;
}

export function pickSeckillSession(list) {
  const items = (list || []).filter((item) => item && item.startTime && item.endTime);
  if (!items.length) {
    return null;
  }
  const now = Date.now();
  const notEnded = items.filter((item) => parseMallTime(item.endTime) > now);
  const pool = notEnded.length ? notEnded : items;
  const start = Math.min(...pool.map((item) => parseMallTime(item.startTime)));
  const end = Math.max(...pool.map((item) => parseMallTime(item.endTime)));
  return { start, end, previewAt: start - PREVIEW_MS };
}

/** wait | preview | live | ended | none */
export function getSessionPhase(session, now = Date.now()) {
  if (!session) {
    return 'none';
  }
  if (now >= session.end) {
    return 'ended';
  }
  if (now >= session.start) {
    return 'live';
  }
  if (now >= session.previewAt) {
    return 'preview';
  }
  return 'wait';
}

export function canEnterSeckillGoods(phase) {
  return phase === 'preview' || phase === 'live' || phase === 'ended';
}

export { PREVIEW_MINUTES };
