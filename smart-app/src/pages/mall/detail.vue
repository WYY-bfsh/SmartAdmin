<template>
  <view class="page">
    <image v-if="item.coverUrl" class="banner" :src="item.coverUrl" mode="aspectFill" />
    <view class="box">
      <view class="price">
        ¥{{ item.seckillPrice }}
        <text class="old">¥{{ item.originPrice }}</text>
      </view>
      <view class="name">{{ item.goodsName }}</view>
      <view class="tag">{{ item.saleStatusDesc }} · 库存 {{ item.stock }} · 每人限 {{ item.perLimit }} 件</view>
      <view class="tag">同时抢购上限 {{ item.concurrentLimit }} 人</view>
      <view v-if="item.remainSeconds" class="tag">剩余 {{ item.remainSeconds }} 秒</view>
      <view class="detail">{{ item.detail }}</view>
    </view>
    <view class="bar">
      <button class="buy" :disabled="item.saleStatus !== 20" @click="buy">
        {{ item.saleStatus === 20 ? '立即秒杀' : item.saleStatusDesc || '暂不可抢' }}
      </button>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const activityId = ref('');
  const item = ref({});

  async function load() {
    const res = await mallH5Api.activityDetail(activityId.value);
    item.value = res.data || {};
  }

  async function buy() {
    if (!getMallToken()) {
      uni.navigateTo({ url: '/pages/mall/login' });
      return;
    }
    try {
      const addrRes = await mallH5Api.addressList();
      const list = addrRes.data || [];
      const addr = list.find((e) => e.defaultFlag) || list[0];
      if (!addr) {
        SmartToast.toast('请先填写收货地址');
        uni.navigateTo({ url: '/pages/mall/address' });
        return;
      }
      const res = await mallH5Api.createOrder({
        activityId: item.value.activityId,
        addressId: addr.addressId,
        qty: 1,
      });
      SmartToast.success('已锁定库存');
      uni.navigateTo({ url: `/pages/mall/order-detail?id=${res.data.orderId}` });
    } catch (e) {
      // toast already shown
    }
  }

  onLoad((options) => {
    activityId.value = options.id;
    load();
  });
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
    padding-bottom: 140rpx;
  }
  .banner {
    width: 100%;
    height: 560rpx;
    background: #eee;
  }
  .box {
    background: #fff;
    padding: 32rpx;
  }
  .price {
    color: #ee0a24;
    font-size: 56rpx;
    font-weight: 800;
  }
  .old {
    color: #999;
    font-size: 26rpx;
    font-weight: 400;
    text-decoration: line-through;
    margin-left: 12rpx;
  }
  .name {
    font-size: 36rpx;
    margin: 16rpx 0;
    font-weight: 600;
  }
  .tag {
    color: #999;
    font-size: 24rpx;
    margin-bottom: 8rpx;
  }
  .detail {
    margin-top: 24rpx;
    color: #555;
    font-size: 28rpx;
    line-height: 1.6;
  }
  .bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 16rpx 24rpx 32rpx;
    background: #fff;
  }
  .buy {
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 48rpx;
    font-size: 32rpx;
  }
  .buy[disabled] {
    background: #ccc;
    color: #fff;
  }
</style>
