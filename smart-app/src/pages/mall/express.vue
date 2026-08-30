<template>
  <view class="page">
    <view class="card">
      <view>{{ order.expressName }} {{ order.waybillNo }}</view>
      <view class="sub">物流轨迹（未配快递100时为演示轨迹）</view>
    </view>
    <view class="card">
      <view class="trace" v-for="(item, idx) in order.traces || []" :key="idx">
        <view class="dot" :class="{ first: idx === 0 }" />
        <view class="content">
          <view class="t">{{ item.ftime }} {{ item.statusText }}</view>
          <view>{{ item.context }}</view>
        </view>
      </view>
      <view v-if="!(order.traces && order.traces.length)" class="empty">暂无物流信息</view>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { mallH5Api } from '@/api/business/mall/mall-h5-api';

  const order = ref({});

  onLoad(async (options) => {
    const res = await mallH5Api.orderDetail(options.id);
    order.value = res.data || {};
  });
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .card {
    background: #fff;
    padding: 28rpx 32rpx;
    margin-bottom: 16rpx;
  }
  .sub,
  .t {
    color: #999;
    font-size: 24rpx;
    margin-top: 8rpx;
  }
  .trace {
    display: flex;
    padding: 20rpx 0;
  }
  .dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background: #ccc;
    margin: 10rpx 20rpx 0 0;
    flex-shrink: 0;
  }
  .dot.first {
    background: #ee0a24;
  }
  .empty {
    color: #999;
    text-align: center;
    padding: 40rpx 0;
  }
</style>
