<template>
  <view class="page">
    <view class="hero" v-if="me.memberId">
      <view class="name">{{ me.nickname || '买家' }}</view>
      <view>{{ me.phone }}</view>
      <view>邀请码 {{ me.inviteCode }}</view>
    </view>
    <view class="hero" v-else @click="goLogin">点击登录</view>
    <view class="menu" @click="go('/pages/mall/address')">收货地址</view>
    <view class="menu" @click="switchOrder">我的订单</view>
    <view class="menu" @click="go('/pages/mall/distribution')">我的分销</view>
    <view class="menu" @click="copyLink">复制邀请链接</view>
    <view v-if="me.memberId" class="menu logout" @click="logout">退出登录</view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken, clearMallToken } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const me = ref({});

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

  function copyLink() {
    const code = me.value.inviteCode || '';
    let url = '';
    // #ifdef H5
    url = `${location.origin}${location.pathname}#/pages/mall/home?invite=${code}`;
    // #endif
    // #ifndef H5
    url = `邀请码 ${code}`;
    // #endif
    uni.setClipboardData({
      data: url,
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
</style>
