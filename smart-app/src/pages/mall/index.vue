<template>
  <view class="page">
    <view class="block">
      <image class="photo rounded" src="/static/images/mall/home-room.jpg" mode="aspectFill" @click="openArticle('company')" />
    </view>

    <view class="section" @click="openArticle('company')">
      <view class="section-title">
        <view class="bar" />
        <text>臻品荟企业介绍</text>
      </view>
      <image class="photo" src="/static/images/mall/home-office2.jpg" mode="aspectFill" />
    </view>

    <view class="section">
      <view class="section-title">
        <view class="bar" />
        <text>合作才能发展，共赢才是王道</text>
      </view>
      <image class="photo" src="/static/images/mall/home-thangka.jpg" mode="aspectFill" @click="openArticle('notice')" />
    </view>
  </view>
</template>

<script setup>
  import { onLoad } from '@dcloudio/uni-app';
  import { MALL_INVITE } from '@/constants/local-storage-key-const';

  onLoad((options) => {
    if (options && options.invite) {
      uni.setStorageSync(MALL_INVITE, options.invite);
    }
  });

  function openArticle(id) {
    uni.navigateTo({ url: `/pages/mall/article?id=${id}` });
  }
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #fff;
    padding-bottom: 24rpx;
  }

  .block {
    padding: 20rpx 24rpx 8rpx;
  }

  .section {
    padding-top: 8rpx;
  }

  .section-title {
    display: flex;
    align-items: center;
    padding: 24rpx 28rpx 20rpx;
    font-size: 32rpx;
    font-weight: 700;
    color: #3d2c14;
  }

  .bar {
    width: 8rpx;
    height: 32rpx;
    margin-right: 14rpx;
    border-radius: 4rpx;
    background: #c49a4c;
  }

  .photo {
    display: block;
    width: 100%;
    height: 380rpx;
    background: #eee;
  }

  .rounded {
    border-radius: 16rpx;
  }
</style>
