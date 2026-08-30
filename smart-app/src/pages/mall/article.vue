<template>
  <view class="page" v-if="article">
    <view v-if="article.id === 'company'" class="article">
      <view class="title">{{ article.title }}</view>
      <view class="time">{{ article.time }}</view>
      <image class="cover" src="/static/images/mall/company-store.jpg" mode="widthFix" />
      <view class="body">
        <view class="lead">公司简介：</view>
        <view class="p">
          臻品荟，是一家专注高端唐卡创作、收藏、鉴赏与文化传播的专业艺术文化企业。深耕传统非遗唐卡艺术领域，立足纯正藏式绘画传承，汇聚资深非遗画师与匠人团队，致力于打造正统、纯净、精工、保真的高端唐卡艺术作品，让千年唐卡文化走向当代、走进大众。
        </view>
        <view class="h">品牌定位</view>
        <view class="p">
          公司坚持「古法传承、匠心精绘、保真溯源、高端雅致」的经营理念，摒弃流水线量产、低价粗制作品，只做纯手绘、矿物质颜料、传统工序的正统精品唐卡。涵盖诸佛菩萨、财神护法、坛城吉祥、山水祥瑞等全系列题材，适配居家供奉、办公室镇场、高端收藏、商务礼赠、文化陈设等多元场景。
        </view>
        <view class="h">核心优势</view>
        <view class="p">1. 正统画师资源：依托专业创作团队与传统绘画规范，保证技法纯正、形神兼备。</view>
        <view class="p">2. 顶级用料工艺：选用金箔、朱砂、石青等天然矿物颜料，多层罩染，色泽沉稳耐久。</view>
        <view class="p">3. 作品保真可溯源：画师落款、收藏证书与来源可追溯，保障收藏安心。</view>
        <view class="p">4. 风格高端适配现代美学：传统唐卡经当代空间优化，适配茶室、会所、宅邸等陈设。</view>
        <view class="h">业务范围</view>
        <view class="p">· 高端手绘唐卡定制</view>
        <view class="p">· 现货收藏与陈设画</view>
        <view class="p">· 企业文化礼品</view>
        <view class="p">· 鉴赏咨询服务</view>
        <view class="p">· 非遗文化传播</view>
      </view>
    </view>

    <view v-else class="article">
      <view class="title">{{ article.title }}</view>
      <view class="time">{{ article.time }}</view>
      <image class="cover" src="/static/images/mall/notice-plan.jpg" mode="widthFix" />
      <view class="body">
        <view class="h">品牌愿景</view>
        <view class="p">
          臻品荟以匠心传文脉，以正品立口碑。让正统、纯净、吉祥、雅致的唐卡艺术，成为当代人修身静心、纳福安居、典藏传家的高端文化之选，传承非遗经典，传递东方极致美学与吉祥文化。
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { getMallHomeArticle } from '@/constants/mall-home-articles';

  const article = ref(null);

  onLoad((options) => {
    const data = getMallHomeArticle(options && options.id);
    if (!data) {
      uni.showToast({ title: '内容不存在', icon: 'none' });
      return;
    }
    article.value = data;
    uni.setNavigationBarTitle({ title: data.navTitle || '公告详情' });
  });
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #fff;
  }

  .article {
    padding-bottom: 60rpx;
  }

  .title {
    padding: 32rpx 32rpx 8rpx;
    font-size: 40rpx;
    font-weight: 700;
    color: #111;
    line-height: 1.4;
  }

  .time {
    padding: 0 32rpx 24rpx;
    font-size: 24rpx;
    color: #999;
  }

  .cover {
    display: block;
    width: 100%;
    background: #f3f3f3;
  }

  .body {
    padding: 36rpx 32rpx 0;
  }

  .lead,
  .h {
    margin-bottom: 16rpx;
    font-size: 32rpx;
    font-weight: 700;
    color: #222;
  }

  .h {
    margin-top: 36rpx;
  }

  .p {
    margin-bottom: 16rpx;
    font-size: 28rpx;
    line-height: 1.8;
    color: #333;
  }
</style>
