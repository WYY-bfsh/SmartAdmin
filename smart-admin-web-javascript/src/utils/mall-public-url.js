/** 把上传文件地址改成当前站点（含端口），避免写成 80 端口导致预览失败。 */
export function resolveUploadUrl(url) {
  if (!url) {
    return '';
  }
  try {
    const parsed = new URL(url, window.location.origin);
    const idx = parsed.pathname.indexOf('/upload/');
    const path = idx >= 0 ? parsed.pathname.substring(idx) : parsed.pathname;
    if (path.startsWith('/upload/')) {
      return `${window.location.origin}${path}`;
    }
  } catch (e) {
    const raw = String(url);
    const idx = raw.indexOf('/upload/');
    if (idx >= 0) {
      return `${window.location.origin}${raw.substring(idx)}`;
    }
  }
  return url;
}

/**
 * 用户端 H5 入口。线上挂在 Nginx /app/，必须带当前端口。
 * 本机 uni 开发服默认 5173，且 manifest 的 router.base 也是 /app/。
 */
export function mallH5HomeUrl() {
  const host = location.hostname;
  if (host === 'localhost' || host === '127.0.0.1') {
    return 'http://localhost:5173/app/#/pages/mall/index';
  }
  return `${location.origin}/app/#/pages/mall/index`;
}
