<template>
  <!-- 订单 Tab：未登录弹层；含待付款/待确认/待发货等筛选 -->
  <view class="page">
    <template v-if="authed">
      <view class="tabs">
        <view v-for="tab in tabs" :key="String(tab.value)" :class="{ on: status === tab.value }" @click="change(tab.value)">
          {{ tab.label }}
        </view>
      </view>
      <view class="card" v-for="item in list" :key="item.orderId" @click="goDetail(item.orderId)">
        <view class="row">
          <text>{{ item.orderNo }}</text>
          <text class="st">{{ statusText(item.orderStatus) }}</text>
        </view>
        <view class="goods">{{ item.goodsName }} × {{ item.qty }}</view>
        <view class="price">¥{{ item.amount }}</view>
      </view>
      <view v-if="!list.length" class="empty">暂无订单</view>
    </template>
    <mall-login-popup v-model="showLogin" @success="onLoginSuccess" />
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken, MALL_ORDER_STATUS } from '@/api/business/mall/mall-h5-api';
  import MallLoginPopup from '@/components/mall-login-popup/index.vue';

  const tabs = [
    { label: '全部', value: undefined },
    { label: '待付款', value: 10 },
    { label: '待确认', value: 15 },
    { label: '待发货', value: 20 },
    { label: '已发货', value: 30 },
    { label: '已完成', value: 40 },
  ];
  const status = ref();
  const list = ref([]);
  const authed = ref(false);
  const showLogin = ref(false);

  function statusText(v) {
    return MALL_ORDER_STATUS[v] || '';
  }

  async function load() {
    if (!getMallToken()) {
      authed.value = false;
      showLogin.value = true;
      list.value = [];
      return;
    }
    authed.value = true;
    showLogin.value = false;
    try {
      const res = await mallH5Api.orderList(status.value);
      list.value = res.data || [];
    } catch (e) {
      list.value = [];
    }
  }

  function onLoginSuccess() {
    load();
  }

  function change(v) {
    status.value = v;
    load();
  }

  function goDetail(id) {
    uni.navigateTo({ url: `/pages/mall/order-detail?id=${id}` });
  }

  onShow(load);
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .tabs {
    display: flex;
    background: #fff;
    padding: 24rpx 0;
    justify-content: space-around;
    font-size: 26rpx;
    color: #666;
  }
  .on {
    color: #ee0a24;
    font-weight: 700;
  }
  .card {
    background: #fff;
    margin: 20rpx 24rpx;
    padding: 24rpx;
    border-radius: 16rpx;
  }
  .row {
    display: flex;
    justify-content: space-between;
    color: #999;
    font-size: 24rpx;
    margin-bottom: 12rpx;
  }
  .st {
    color: #ee0a24;
  }
  .goods {
    font-size: 28rpx;
  }
  .price {
    text-align: right;
    font-weight: 700;
    margin-top: 16rpx;
    font-size: 32rpx;
  }
  .empty {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
  }
</style>
