import { MALL_INVITE } from '@/constants/local-storage-key-const';

function readInviteFromSearch() {
  // #ifdef H5
  try {
    if (typeof location !== 'undefined') {
      return new URLSearchParams(location.search).get('invite') || '';
    }
  } catch (e) {
    return '';
  }
  // #endif
  return '';
}

export function captureMallInvite(options) {
  let invite = (options && options.invite) || '';
  if (!invite) {
    invite = readInviteFromSearch();
  }
  invite = String(invite || uni.getStorageSync(MALL_INVITE) || '').trim();
  if (invite) {
    uni.setStorageSync(MALL_INVITE, invite);
  }
  return invite;
}

export function buildRegisterInviteUrl(inviteCode) {
  const code = encodeURIComponent(String(inviteCode || '').trim());
  // #ifdef H5
  if (typeof location !== 'undefined') {
    return `${location.origin}${location.pathname}?invite=${code}#/pages/mall/register?invite=${code}`;
  }
  // #endif
  return '';
}
