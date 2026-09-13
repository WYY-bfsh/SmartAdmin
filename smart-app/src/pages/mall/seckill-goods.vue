<template>
  <!-- 秒杀商品列表。开售前 30 分钟起可进；预览期不能下单 -->
  <view class="page">
    <view v-if="previewing" class="banner">预览中 · 开售后才能下单</view>
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
        <view class="btn" :class="{ off: item.saleStatus !== 20 }">{{ actionText(item) }}</view>
      </view>
    </view>
    <view v-if="!list.length && !apiHint" class="empty">暂无秒杀活动</view>
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken } from '@/api/business/mall/mall-h5-api';
  import { canEnterSeckillGoods, getSessionPhase, pickSeckillSession } from '@/utils/mall-seckill';
  import { SmartToast } from '@/lib/smart-support';

  const list = ref([]);
  const apiHint = ref('');
  const routePhase = ref('');

  const previewing = computed(() => {
    const session = pickSeckillSession(list.value);
    const phase = getSessionPhase(session) || routePhase.value;
    return phase === 'preview';
  });

  function actionText(item) {
    if (item.saleStatus === 20) {
      return '立即抢';
    }
    if (item.saleStatus === 10) {
      return '预览';
    }
    return item.saleStatusDesc;
  }

  function guard() {
    if (!getMallToken()) {
      uni.switchTab({ url: '/pages/mall/home' });
      return false;
    }
    const session = pickSeckillSession(list.value);
    const phase = getSessionPhase(session);
    if (session && !canEnterSeckillGoods(phase)) {
      SmartToast.toast('开售前30分钟可预览商品');
      uni.switchTab({ url: '/pages/mall/home' });
      return false;
    }
    return true;
  }

  async function load() {
    if (!getMallToken()) {
      uni.switchTab({ url: '/pages/mall/home' });
      return;
    }
    apiHint.value = '';
    try {
      const res = await mallH5Api.activityList();
      list.value = res.data || [];
      if (!guard()) {
        return;
      }
    } catch (e) {
      list.value = [];
      apiHint.value = '活动加载失败，点此重试';
    }
  }

  function goDetail(id) {
    uni.navigateTo({ url: `/pages/mall/detail?id=${id}` });
  }

  onLoad((options) => {
    routePhase.value = (options && options.phase) || '';
  });

  onShow(load);
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
    padding-bottom: 30rpx;
  }
  .banner {
    margin: 20rpx 24rpx 0;
    padding: 16rpx 20rpx;
    background: #fff3f0;
    color: #ee0a24;
    border-radius: 12rpx;
    font-size: 24rpx;
    text-align: center;
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
