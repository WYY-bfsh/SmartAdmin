<template>
  <!-- 商品详情：未开始仅预览；开售后可选数量（一单一种商品） -->
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
      <view v-if="countdownHint" class="count">{{ countdownHint }}</view>
      <view class="qty-row" v-if="canBuy">
        <text>数量</text>
        <view class="stepper">
          <view class="step" @click="changeQty(-1)">-</view>
          <text class="num">{{ qty }}</text>
          <view class="step" @click="changeQty(1)">+</view>
        </view>
        <text class="qty-tip">每次仅限本商品，最多 {{ maxQty }} 件</text>
      </view>
      <view class="detail">{{ item.detail }}</view>
    </view>
    <view class="bar">
      <button class="buy" :disabled="!canBuy" @click="buy">{{ buyText }}</button>
    </view>
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { onHide, onLoad, onShow, onUnload } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken } from '@/api/business/mall/mall-h5-api';
  import { formatRemain, parseMallTime } from '@/utils/mall-seckill';
  import { SmartToast } from '@/lib/smart-support';

  const activityId = ref('');
  const item = ref({});
  const qty = ref(1);
  const now = ref(Date.now());
  let timer = null;

  const localStatus = computed(() => {
    const start = parseMallTime(item.value.startTime);
    const end = parseMallTime(item.value.endTime);
    if (!start || !end) {
      return item.value.saleStatus;
    }
    if (now.value < start) {
      return 10;
    }
    if (now.value > end || item.value.stock <= 0) {
      return 30;
    }
    return 20;
  });

  const canBuy = computed(() => localStatus.value === 20);

  const maxQty = computed(() => {
    const stock = Number(item.value.stock) || 0;
    const limit = Number(item.value.perLimit) || 1;
    const n = Math.min(stock, limit);
    return n > 0 ? n : 1;
  });

  const buyText = computed(() => {
    if (localStatus.value === 20) {
      return '立即秒杀';
    }
    if (localStatus.value === 10) {
      return '未开始，仅可预览';
    }
    return item.value.saleStatusDesc || '暂不可抢';
  });

  const countdownHint = computed(() => {
    const start = parseMallTime(item.value.startTime);
    const end = parseMallTime(item.value.endTime);
    if (!start || !end) {
      return '';
    }
    if (now.value < start) {
      return `距开售 ${formatRemain(start - now.value)}`;
    }
    if (now.value < end && item.value.stock > 0) {
      return `距结束 ${formatRemain(end - now.value)}`;
    }
    return '';
  });

  function tick() {
    now.value = Date.now();
  }

  function startTimer() {
    stopTimer();
    tick();
    timer = setInterval(tick, 1000);
  }

  function stopTimer() {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  async function load() {
    const res = await mallH5Api.activityDetail(activityId.value);
    item.value = res.data || {};
    if (qty.value > maxQty.value) {
      qty.value = maxQty.value;
    }
    startTimer();
  }

  function changeQty(delta) {
    const next = qty.value + delta;
    if (next < 1 || next > maxQty.value) {
      return;
    }
    qty.value = next;
  }

  async function buy() {
    if (!canBuy.value) {
      SmartToast.toast('开售前仅可预览，不能下单');
      return;
    }
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
        qty: qty.value,
      });
      SmartToast.success('已锁定库存');
      uni.navigateTo({ url: `/pages/mall/order-detail?id=${res.data.orderId}` });
    } catch (e) {
      // toast already shown
    }
  }

  onLoad((options) => {
    activityId.value = options.id;
  });

  onShow(() => {
    if (activityId.value) {
      load();
    }
  });
  onHide(stopTimer);
  onUnload(stopTimer);
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
  .count {
    margin: 16rpx 0 8rpx;
    color: #ee0a24;
    font-size: 28rpx;
    font-weight: 700;
  }
  .qty-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-top: 28rpx;
    font-size: 28rpx;
    color: #333;
    gap: 16rpx;
  }
  .stepper {
    display: flex;
    align-items: center;
    border: 1px solid #eee;
    border-radius: 8rpx;
    overflow: hidden;
  }
  .step {
    width: 56rpx;
    height: 56rpx;
    text-align: center;
    line-height: 56rpx;
    background: #f7f7f7;
    font-size: 32rpx;
  }
  .num {
    width: 72rpx;
    text-align: center;
    font-weight: 700;
  }
  .qty-tip {
    font-size: 22rpx;
    color: #999;
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
