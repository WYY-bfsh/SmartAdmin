<template>
  <view class="page">
    <view class="card" v-if="order.orderId">
      <view class="st">{{ statusText(order.orderStatus) }}</view>
      <view>{{ order.goodsName }} × {{ order.qty }}</view>
      <view class="price">¥{{ order.amount }}</view>
      <view class="addr">{{ order.receiverName }} {{ order.receiverPhone }}\n{{ order.receiverAddress }}</view>
      <view v-if="order.waybillNo" class="logi" @click="goExpress">
        {{ order.expressName }} {{ order.waybillNo }} · 查看物流
      </view>
    </view>
    <view class="bar">
      <button v-if="order.orderStatus === 10" class="buy" @click="pay">立即付款（演示）</button>
      <button v-if="order.orderStatus === 30" class="buy" @click="receive">确认收货</button>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { mallH5Api, MALL_ORDER_STATUS } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const orderId = ref('');
  const order = ref({});

  function statusText(v) {
    return MALL_ORDER_STATUS[v] || '';
  }

  async function load() {
    const res = await mallH5Api.orderDetail(orderId.value);
    order.value = res.data || {};
  }

  async function pay() {
    try {
      await mallH5Api.pay(order.value.orderId);
      SmartToast.success('支付成功');
      load();
    } catch (e) {
      // toast already shown
    }
  }

  async function receive() {
    try {
      await mallH5Api.receive(order.value.orderId);
      SmartToast.success('已确认收货');
      load();
    } catch (e) {
      // toast already shown
    }
  }

  function goExpress() {
    uni.navigateTo({ url: `/pages/mall/express?id=${order.value.orderId}` });
  }

  onLoad((options) => {
    orderId.value = options.id;
    load();
  });
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .card {
    background: #fff;
    margin: 24rpx;
    padding: 32rpx;
    border-radius: 16rpx;
    white-space: pre-line;
  }
  .st {
    color: #ee0a24;
    font-weight: 700;
    margin-bottom: 16rpx;
    font-size: 32rpx;
  }
  .price {
    font-size: 40rpx;
    font-weight: 800;
    margin: 16rpx 0;
  }
  .addr,
  .logi {
    color: #666;
    font-size: 26rpx;
    margin-top: 24rpx;
  }
  .bar {
    padding: 24rpx;
  }
  .buy {
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 48rpx;
  }
</style>
