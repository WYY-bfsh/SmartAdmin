<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">限时秒杀</view>
      <view class="hero-sub">真货发货 · 一级分销</view>
    </view>
    <view v-if="apiHint" class="hint" @click="load">{{ apiHint }}</view>
    <view class="card" v-for="item in list" :key="item.activityId" @click="goDetail(item.activityId)">
      <image class="cover" :src="item.coverUrl" mode="aspectFill" />
      <view class="info">
        <view class="name">{{ item.goodsName }}</view>
        <view class="price-row">
          <text class="now">¥{{ item.seckillPrice }}</text>
          <text class="old">¥{{ item.originPrice }}</text>
        </view>
        <view class="meta">{{ item.saleStatusDesc }} · 剩余 {{ item.stock }} · 同时限 {{ item.concurrentLimit }} 人</view>
        <view class="btn" :class="{ off: item.saleStatus !== 20 }">{{ item.saleStatus === 20 ? '立即抢' : item.saleStatusDesc }}</view>
      </view>
    </view>
    <view v-if="!list.length && !apiHint" class="empty">暂无秒杀活动</view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow, onLoad } from '@dcloudio/uni-app';
  import { mallH5Api } from '@/api/business/mall/mall-h5-api';
  import { captureMallInvite } from '@/utils/mall-invite';

  const list = ref([]);
  const apiHint = ref('');

  onLoad((options) => {
    captureMallInvite(options);
  });

  async function load() {
    apiHint.value = '';
    try {
      const res = await mallH5Api.activityList();
      list.value = res.data || [];
    } catch (e) {
      list.value = [];
      if (e && e.code === 10001) {
        apiHint.value = '后端还是旧进程，点此重试。请先重启 AdminApplication。';
      } else {
        apiHint.value = '活动加载失败，点此重试';
      }
    }
  }

  function goDetail(id) {
    uni.navigateTo({ url: `/pages/mall/detail?id=${id}` });
  }

  onShow(load);
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
    padding-bottom: 30rpx;
  }
  .hero {
    background: linear-gradient(135deg, #ee0a24, #ff6a3d);
    color: #fff;
    padding: 36rpx 32rpx 48rpx;
  }
  .hero-title {
    font-size: 44rpx;
    font-weight: 800;
  }
  .hero-sub {
    margin-top: 8rpx;
    opacity: 0.9;
    font-size: 26rpx;
  }
  .hint {
    margin: 20rpx 24rpx;
    padding: 20rpx;
    background: #fff3f0;
    color: #ee0a24;
    border-radius: 12rpx;
    font-size: 24rpx;
  }
  .card {
    display: flex;
    background: #fff;
    margin: 20rpx 24rpx;
    border-radius: 16rpx;
    overflow: hidden;
  }
  .cover {
    width: 220rpx;
    height: 220rpx;
    flex-shrink: 0;
    background: #eee;
  }
  .info {
    flex: 1;
    padding: 20rpx 20rpx 16rpx;
    position: relative;
  }
  .name {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
  }
  .price-row {
    margin-top: 12rpx;
  }
  .now {
    color: #ee0a24;
    font-size: 36rpx;
    font-weight: 800;
    margin-right: 12rpx;
  }
  .old {
    color: #999;
    font-size: 24rpx;
    text-decoration: line-through;
  }
  .meta {
    color: #999;
    font-size: 22rpx;
    margin-top: 10rpx;
  }
  .btn {
    position: absolute;
    right: 16rpx;
    bottom: 16rpx;
    background: #ee0a24;
    color: #fff;
    font-size: 22rpx;
    padding: 8rpx 20rpx;
    border-radius: 28rpx;
  }
  .btn.off {
    background: #ccc;
  }
  .empty {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
  }
</style>
