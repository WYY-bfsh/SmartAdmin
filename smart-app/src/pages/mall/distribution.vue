<template>
  <view class="page">
    <view class="hero">
      <view>待结算 ¥{{ me.frozenCommission || 0 }}</view>
      <view>已结算 ¥{{ me.settledCommission || 0 }}</view>
      <view>团队 {{ me.teamCount || 0 }} 人 · 邀请码 {{ me.inviteCode }}</view>
    </view>
    <view class="card">
      <view class="h">我的下级</view>
      <view v-for="item in team" :key="item.memberId" class="row">
        <image v-if="item.avatar" class="tiny" :src="fileUrl(item.avatar)" mode="aspectFill" />
        <view v-else class="tiny fallback">{{ (item.nickname || '买').slice(0, 1) }}</view>
        <text>{{ item.nickname }} {{ item.phone }}</text>
      </view>
      <view v-if="!team.length" class="empty">还没有下级，把邀请链接发给好友</view>
    </view>
    <view class="card">
      <view class="h">佣金明细</view>
      <view v-for="item in list" :key="item.commissionId" class="row">
        {{ item.orderNo }} · ¥{{ item.amount }} · {{ item.status === 20 ? '已结算' : '待结算' }}
      </view>
      <view v-if="!list.length" class="empty">暂无佣金</view>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken, resolveMallFileUrl } from '@/api/business/mall/mall-h5-api';

  function fileUrl(url) {
    return resolveMallFileUrl(url);
  }

  const me = ref({});
  const team = ref([]);
  const list = ref([]);

  onShow(async () => {
    if (!getMallToken()) {
      uni.navigateTo({ url: '/pages/mall/login' });
      return;
    }
    const meRes = await mallH5Api.me();
    me.value = meRes.data || {};
    const teamRes = await mallH5Api.team();
    team.value = teamRes.data || [];
    const cRes = await mallH5Api.commission({ pageNum: 1, pageSize: 50 });
    list.value = (cRes.data && cRes.data.list) || [];
  });
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .hero {
    background: #ee0a24;
    color: #fff;
    padding: 40rpx 32rpx;
    line-height: 1.8;
  }
  .card {
    background: #fff;
    margin: 24rpx;
    padding: 24rpx;
    border-radius: 16rpx;
  }
  .h {
    font-weight: 700;
    margin-bottom: 12rpx;
  }
  .row {
    display: flex;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1px solid #f5f5f5;
    font-size: 26rpx;
  }
  .tiny {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    margin-right: 16rpx;
    background: #f3f3f3;
    flex-shrink: 0;
  }
  .fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22rpx;
    color: #888;
  }
  .empty {
    color: #999;
    font-size: 24rpx;
    padding: 20rpx 0;
  }
</style>
