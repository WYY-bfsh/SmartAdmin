<template>
  <view class="page">
    <view class="hero" @click="openArticle('company')">
      <image class="hero-img" src="/static/images/mall/home-room.jpg" mode="aspectFill" />
      <view class="hero-mask">
        <view class="brand">臻品荟</view>
        <view class="slogan">正统唐卡 · 真货发货 · 限时秒杀</view>
      </view>
    </view>

    <view class="entries">
      <view class="entry" @click="goTab('/pages/mall/home')">
        <view class="entry-icon red">抢</view>
        <text>限时秒杀</text>
      </view>
      <view class="entry" @click="goTab('/pages/mall/order')">
        <view class="entry-icon gold">单</view>
        <text>我的订单</text>
      </view>
      <view class="entry" @click="goTab('/pages/mall/mine')">
        <view class="entry-icon brown">分</view>
        <text>我的分销</text>
      </view>
      <view class="entry" @click="openArticle('company')">
        <view class="entry-icon dark">馆</view>
        <text>企业介绍</text>
      </view>
    </view>

    <view class="notice" @click="openArticle('notice')">
      <text class="notice-tag">公告</text>
      <text class="notice-text">合作才能发展，共赢才是王道</text>
      <text class="notice-more">详情</text>
    </view>

    <view class="section-head">
      <view class="bar" />
      <text>品牌故事</text>
    </view>

    <view class="story" @click="openArticle('company')">
      <image class="story-cover" src="/static/images/mall/home-office2.jpg" mode="aspectFill" />
      <view class="story-body">
        <view class="story-title">臻品荟企业介绍</view>
        <view class="story-desc">专注高端唐卡创作、收藏、鉴赏与文化传播。古法传承、匠心精绘、保真溯源。</view>
        <view class="story-link">阅读全文</view>
      </view>
    </view>

    <view class="story" @click="openArticle('notice')">
      <image class="story-cover" src="/static/images/mall/home-thangka.jpg" mode="aspectFill" />
      <view class="story-body">
        <view class="story-title">合作才能发展，共赢才是王道</view>
        <view class="story-desc">以匠心传文脉，以正品立口碑，让正统唐卡成为修身静心、典藏传家的文化之选。</view>
        <view class="story-link">阅读全文</view>
      </view>
    </view>

    <view class="foot">臻品荟 · 非遗唐卡 · 限时秒杀</view>
  </view>
</template>

<script setup>
  import { onLoad } from '@dcloudio/uni-app';
  import { captureMallInvite } from '@/utils/mall-invite';

  onLoad((options) => {
    captureMallInvite(options);
  });

  function openArticle(id) {
    uni.navigateTo({ url: `/pages/mall/article?id=${id}` });
  }

  function goTab(url) {
    uni.switchTab({ url });
  }
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f6f1e8;
    padding-bottom: 48rpx;
  }

  .hero {
    position: relative;
    height: 420rpx;
    overflow: hidden;
  }

  .hero-img {
    width: 100%;
    height: 100%;
    display: block;
  }

  .hero-mask {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, rgba(20, 12, 6, 0.15) 0%, rgba(20, 12, 6, 0.72) 100%);
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    padding: 40rpx 36rpx 48rpx;
  }

  .brand {
    color: #f4e4c1;
    font-size: 56rpx;
    font-weight: 800;
    letter-spacing: 8rpx;
  }

  .slogan {
    margin-top: 12rpx;
    color: rgba(255, 255, 255, 0.88);
    font-size: 26rpx;
    letter-spacing: 2rpx;
  }

  .entries {
    margin: -36rpx 24rpx 0;
    position: relative;
    z-index: 1;
    background: #fff;
    border-radius: 20rpx;
    padding: 28rpx 8rpx 24rpx;
    display: flex;
    justify-content: space-around;
    box-shadow: 0 12rpx 32rpx rgba(80, 50, 20, 0.08);
  }

  .entry {
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 22rpx;
    color: #5a4630;
  }

  .entry-icon {
    width: 80rpx;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    border-radius: 24rpx;
    color: #fff;
    font-size: 32rpx;
    font-weight: 700;
    margin-bottom: 12rpx;
  }

  .entry-icon.red {
    background: linear-gradient(135deg, #ee0a24, #ff6a3d);
  }

  .entry-icon.gold {
    background: linear-gradient(135deg, #c49a4c, #e0c07a);
  }

  .entry-icon.brown {
    background: linear-gradient(135deg, #8b5a2b, #c49a4c);
  }

  .entry-icon.dark {
    background: linear-gradient(135deg, #3d2c14, #6b4e2e);
  }

  .notice {
    margin: 24rpx 24rpx 8rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 22rpx 24rpx;
    display: flex;
    align-items: center;
  }

  .notice-tag {
    flex-shrink: 0;
    font-size: 22rpx;
    color: #c49a4c;
    font-weight: 700;
    margin-right: 16rpx;
    padding-right: 16rpx;
    border-right: 1px solid #eee;
  }

  .notice-text {
    flex: 1;
    font-size: 26rpx;
    color: #333;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .notice-more {
    flex-shrink: 0;
    margin-left: 12rpx;
    font-size: 22rpx;
    color: #c49a4c;
  }

  .section-head {
    display: flex;
    align-items: center;
    padding: 32rpx 28rpx 16rpx;
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

  .story {
    margin: 0 24rpx 24rpx;
    background: #fff;
    border-radius: 20rpx;
    overflow: hidden;
    box-shadow: 0 8rpx 24rpx rgba(80, 50, 20, 0.05);
  }

  .story-cover {
    display: block;
    width: 100%;
    height: 280rpx;
    background: #eee;
  }

  .story-body {
    padding: 24rpx 28rpx 28rpx;
  }

  .story-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #2a1c0e;
  }

  .story-desc {
    margin-top: 12rpx;
    font-size: 24rpx;
    line-height: 1.7;
    color: #7a6a55;
  }

  .story-link {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #c49a4c;
    font-weight: 600;
  }

  .foot {
    text-align: center;
    color: #b8a58d;
    font-size: 22rpx;
    padding: 12rpx 0 8rpx;
  }
</style>
