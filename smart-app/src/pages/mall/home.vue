<template>
  <!-- 抢购 Tab：未登录弹层；已登录展示开售倒计时，预览窗口内可进商品列表 -->
  <view class="page">
    <view class="hero">
      <view class="hero-title">限时秒杀</view>
      <view class="hero-sub">真货发货 · 一级分销</view>
    </view>
    <template v-if="authed">
      <view v-if="apiHint" class="hint" @click="load">{{ apiHint }}</view>
      <view v-else-if="phase === 'none'" class="empty">暂无秒杀活动</view>
      <view v-else class="board">
        <view class="label">{{ countdownLabel }}</view>
        <view class="clock">{{ countdownText }}</view>
        <view v-if="session" class="range">本场 {{ formatClock(session.start) }} — {{ formatClock(session.end) }}</view>
        <view class="tip">开售前 {{ previewMinutes }} 分钟可进入商品页预览，开售后才能抢购</view>
        <button class="enter" :disabled="!canEnter" @click="enterGoods">{{ enterText }}</button>
      </view>
    </template>
    <mall-login-popup v-model="showLogin" @success="onLoginSuccess" />
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { onHide, onLoad, onShow, onUnload } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken } from '@/api/business/mall/mall-h5-api';
  import { captureMallInvite } from '@/utils/mall-invite';
  import {
    PREVIEW_MINUTES,
    canEnterSeckillGoods,
    formatClock,
    formatRemain,
    getSessionPhase,
    pickSeckillSession,
  } from '@/utils/mall-seckill';
  import MallLoginPopup from '@/components/mall-login-popup/index.vue';
  import { SmartToast } from '@/lib/smart-support';

  const list = ref([]);
  const apiHint = ref('');
  const authed = ref(false);
  const showLogin = ref(false);
  const now = ref(Date.now());
  const previewMinutes = PREVIEW_MINUTES;
  let timer = null;

  const session = computed(() => pickSeckillSession(list.value));
  const phase = computed(() => getSessionPhase(session.value, now.value));
  const canEnter = computed(() => canEnterSeckillGoods(phase.value));

  const countdownLabel = computed(() => {
    if (phase.value === 'live') {
      return '距结束';
    }
    if (phase.value === 'ended') {
      return '本场已结束';
    }
    return '距开售';
  });

  const countdownText = computed(() => {
    if (!session.value) {
      return '00:00:00';
    }
    if (phase.value === 'ended') {
      return '00:00:00';
    }
    const target = phase.value === 'live' ? session.value.end : session.value.start;
    return formatRemain(target - now.value);
  });

  const enterText = computed(() => {
    if (phase.value === 'wait') {
      return `${previewMinutes}分钟后可预览`;
    }
    if (phase.value === 'preview') {
      return '预览商品';
    }
    if (phase.value === 'live') {
      return '进入抢购';
    }
    if (phase.value === 'ended') {
      return '查看商品';
    }
    return '进入';
  });

  onLoad((options) => {
    captureMallInvite(options);
  });

  function gate() {
    if (!getMallToken()) {
      authed.value = false;
      showLogin.value = true;
      list.value = [];
      apiHint.value = '';
      stopTimer();
      return false;
    }
    authed.value = true;
    showLogin.value = false;
    return true;
  }

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
    if (!gate()) {
      return;
    }
    apiHint.value = '';
    try {
      const res = await mallH5Api.activityList();
      list.value = res.data || [];
      startTimer();
    } catch (e) {
      list.value = [];
      stopTimer();
      if (e && e.code === 10001) {
        apiHint.value = '后端还是旧进程，点此重试。请先重启 AdminApplication。';
      } else {
        apiHint.value = '活动加载失败，点此重试';
      }
    }
  }

  function onLoginSuccess() {
    load();
  }

  function enterGoods() {
    if (!canEnter.value) {
      SmartToast.toast(`开售前${previewMinutes}分钟可预览商品`);
      return;
    }
    uni.navigateTo({ url: `/pages/mall/seckill-goods?phase=${phase.value}` });
  }

  onShow(load);
  onHide(stopTimer);
  onUnload(stopTimer);
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
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
  .board {
    margin: 32rpx 24rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 56rpx 32rpx 48rpx;
    text-align: center;
  }
  .label {
    font-size: 26rpx;
    color: #888;
  }
  .clock {
    margin-top: 16rpx;
    font-size: 64rpx;
    font-weight: 800;
    color: #ee0a24;
    letter-spacing: 4rpx;
    font-variant-numeric: tabular-nums;
  }
  .range {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #333;
  }
  .tip {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;
    line-height: 1.6;
  }
  .enter {
    margin-top: 48rpx;
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 44rpx;
  }
  .enter[disabled] {
    background: #ccc;
    color: #fff;
  }
  .empty {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
  }
</style>
