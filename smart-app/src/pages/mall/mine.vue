<template>
  <view class="page">
    <view class="hero" v-if="me.memberId">
      <view class="profile">
        <image v-if="avatarUrl" class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view v-else class="avatar fallback">{{ (me.nickname || '买').slice(0, 1) }}</view>
        <view class="meta">
          <view class="name">{{ me.nickname || '买家' }}</view>
          <view>{{ me.phone }}</view>
          <view>邀请码 {{ me.inviteCode }}</view>
        </view>
      </view>
    </view>
    <view class="hero" v-else @click="goLogin">点击登录</view>
    <view class="menu" @click="go('/pages/mall/address')">收货地址</view>
    <view class="menu" @click="switchOrder">我的订单</view>
    <view class="menu" @click="go('/pages/mall/distribution')">我的分销</view>
    <view class="menu" @click="showInviteQr">邀请二维码</view>
    <view v-if="me.memberId" class="menu logout" @click="logout">退出登录</view>

    <view v-if="qrVisible" class="qr-mask" @click="closeInviteQr">
      <view class="qr-card" @click.stop>
        <view class="qr-title">邀请好友注册</view>
        <image v-if="qrImage" class="qr-image" :src="qrImage" mode="aspectFit" />
        <view v-else class="qr-loading">二维码生成中…</view>
        <view class="qr-code">邀请码 {{ me.inviteCode }}</view>
        <view class="qr-tip">微信扫码后进入注册页，邀请码会自动带上</view>
        <view class="qr-actions">
          <view class="qr-btn ghost" @click="copyInviteUrl">复制链接</view>
          <view class="qr-btn" @click="closeInviteQr">关闭</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { onShow } from '@dcloudio/uni-app';
  import { computed, ref } from 'vue';
  import { mallH5Api, getMallToken, clearMallToken, resolveMallFileUrl } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';
  import { buildRegisterInviteUrl } from '@/utils/mall-invite';
  // #ifdef H5
  import QRCode from 'qrcode/lib/browser.js';
  // #endif

  const me = ref({});
  const avatarUrl = computed(() => resolveMallFileUrl(me.value.avatar));
  const qrVisible = ref(false);
  const qrImage = ref('');
  const inviteUrl = ref('');

  async function load() {
    if (!getMallToken()) {
      me.value = {};
      return;
    }
    try {
      const res = await mallH5Api.me();
      me.value = res.data || {};
    } catch (e) {
      me.value = {};
    }
  }

  function goLogin() {
    uni.navigateTo({ url: '/pages/mall/login' });
  }

  function go(url) {
    if (!getMallToken()) {
      goLogin();
      return;
    }
    uni.navigateTo({ url });
  }

  function switchOrder() {
    if (!getMallToken()) {
      goLogin();
      return;
    }
    uni.switchTab({ url: '/pages/mall/order' });
  }

  function getInviteUrl() {
    const code = me.value.inviteCode || '';
    const url = buildRegisterInviteUrl(code);
    if (url) {
      return url;
    }
    return code ? `邀请码 ${code}` : '';
  }

  async function showInviteQr() {
    if (!getMallToken()) {
      goLogin();
      return;
    }
    const code = me.value.inviteCode || '';
    if (!code) {
      SmartToast.toast('暂无邀请码');
      return;
    }
    const url = getInviteUrl();
    inviteUrl.value = url;
    qrImage.value = '';
    qrVisible.value = true;
    // #ifdef H5
    try {
      qrImage.value = await QRCode.toDataURL(url, {
        width: 280,
        margin: 1,
        errorCorrectionLevel: 'M',
        color: { dark: '#111111', light: '#ffffff' },
      });
    } catch (e) {
      qrVisible.value = false;
      SmartToast.toast('二维码生成失败');
    }
    // #endif
    // #ifndef H5
    SmartToast.toast('请在 H5 中打开以展示邀请二维码');
    qrVisible.value = false;
    // #endif
  }

  function closeInviteQr() {
    qrVisible.value = false;
  }

  function copyInviteUrl() {
    const data = inviteUrl.value || getInviteUrl();
    if (!data) {
      SmartToast.toast('暂无邀请链接');
      return;
    }
    uni.setClipboardData({
      data,
      success: () => SmartToast.success('已复制'),
    });
  }

  function logout() {
    clearMallToken();
    me.value = {};
    SmartToast.toast('已退出');
  }

  onShow(load);
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .hero {
    background: linear-gradient(90deg, #ee0a24, #ff6a3d);
    color: #fff;
    padding: 88rpx 32rpx 56rpx;
    padding-top: calc(88rpx + env(safe-area-inset-top));
    line-height: 1.7;
  }
  .profile {
    display: flex;
    align-items: center;
  }
  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    margin-right: 24rpx;
    background: rgba(255, 255, 255, 0.2);
    flex-shrink: 0;
  }
  .fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    font-weight: 700;
  }
  .meta {
    min-width: 0;
  }
  .name {
    font-size: 40rpx;
    font-weight: 700;
    margin-bottom: 8rpx;
  }
  .menu {
    background: #fff;
    padding: 32rpx;
    border-bottom: 1px solid #f3f3f3;
    font-size: 30rpx;
  }
  .logout {
    color: #ee0a24;
    margin-top: 20rpx;
    text-align: center;
  }
  .qr-mask {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 1200;
    background: rgba(0, 0, 0, 0.55);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48rpx;
    box-sizing: border-box;
  }
  .qr-card {
    width: 100%;
    max-width: 560rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 36rpx 32rpx;
    box-sizing: border-box;
    text-align: center;
  }
  .qr-title {
    font-size: 34rpx;
    font-weight: 700;
    color: #111;
  }
  .qr-image {
    width: 400rpx;
    height: 400rpx;
    margin: 28rpx auto 0;
    display: block;
  }
  .qr-loading {
    height: 400rpx;
    line-height: 400rpx;
    color: #999;
    font-size: 26rpx;
  }
  .qr-code {
    margin-top: 8rpx;
    font-size: 28rpx;
    color: #333;
    font-weight: 600;
  }
  .qr-tip {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #999;
    line-height: 1.5;
  }
  .qr-actions {
    display: flex;
    gap: 16rpx;
    margin-top: 32rpx;
  }
  .qr-btn {
    flex: 1;
    height: 72rpx;
    line-height: 72rpx;
    background: #ee0a24;
    color: #fff;
    border-radius: 36rpx;
    font-size: 26rpx;
  }
  .qr-btn.ghost {
    background: #f5f5f5;
    color: #333;
  }
</style>
