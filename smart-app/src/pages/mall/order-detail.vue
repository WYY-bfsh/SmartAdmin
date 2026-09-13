<template>
  <!-- 订单详情：待付款展示商家收款码并上传凭证；待确认等待商家审核 -->
  <view class="page">
    <view class="card" v-if="order.orderId">
      <view class="st">{{ statusText(order.orderStatus) }}</view>
      <view class="row">订单号 {{ order.orderNo }}</view>
      <view>{{ order.goodsName }} × {{ order.qty }}</view>
      <view class="price">应付 ¥{{ order.amount }}</view>
      <view class="addr">{{ order.receiverName }} {{ order.receiverPhone }}\n{{ order.receiverAddress }}</view>
      <view v-if="order.waybillNo" class="logi" @click="goExpress">
        {{ order.expressName }} {{ order.waybillNo }} · 查看物流
      </view>
    </view>

    <view class="card" v-if="order.orderStatus === 10">
      <view class="sub">请扫商家收款码转账</view>
      <view v-if="!hasPayQr" class="warn">商家尚未配置收款码，请稍后再支付</view>
      <view class="qr-row">
        <view class="qr-item">
          <image v-if="wechatQr" class="qr" :src="wechatQr" mode="aspectFit" />
          <view v-else class="qr empty">未配置</view>
          <view class="qr-tip">微信收款码</view>
        </view>
        <view class="qr-item">
          <image v-if="alipayQr" class="qr" :src="alipayQr" mode="aspectFit" />
          <view v-else class="qr empty">未配置</view>
          <view class="qr-tip">支付宝收款码</view>
        </view>
      </view>
      <view class="sub">转账后上传付款截图</view>
      <view class="upload" @click="chooseProof">
        <image v-if="proofPreview" class="proof" :src="proofPreview" mode="aspectFit" />
        <view v-else class="proof empty">{{ uploading ? '上传中…' : '+ 付款截图' }}</view>
      </view>
      <textarea class="note" v-model="payNote" maxlength="255" placeholder="填写付款说明（选填），如转账人姓名、方式" />
    </view>

    <view class="card" v-if="order.orderStatus === 15">
      <view class="sub">已提交，等待商家确认收款</view>
      <image v-if="proofPreview" class="proof wide" :src="proofPreview" mode="aspectFit" />
      <view v-if="order.payNote" class="addr">说明：{{ order.payNote }}</view>
    </view>

    <view class="card" v-if="order.orderStatus === 50">
      <view class="sub">订单已关闭</view>
      <view class="addr">{{ order.remark || '超时未付或商家未确认收款' }}</view>
    </view>

    <view class="bar">
      <button v-if="order.orderStatus === 10" class="buy" :disabled="uploading || submitting || !hasPayQr" @click="submitProof">
        {{ submitting ? '提交中…' : '我已付款，提交凭证' }}
      </button>
      <button v-if="order.orderStatus === 30" class="buy" @click="receive">确认收货</button>
    </view>
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import { mallH5Api, MALL_ORDER_STATUS, resolveMallFileUrl } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const orderId = ref('');
  const order = ref({});
  const config = ref({});
  const payProofUrl = ref('');
  const payNote = ref('');
  const uploading = ref(false);
  const submitting = ref(false);

  const wechatQr = computed(() => resolveMallFileUrl(config.value.merchantWechatQr));
  const alipayQr = computed(() => resolveMallFileUrl(config.value.merchantAlipayQr));
  const hasPayQr = computed(() => !!(wechatQr.value || alipayQr.value));
  const proofPreview = computed(() => resolveMallFileUrl(payProofUrl.value || order.value.payProofUrl));

  function statusText(v) {
    return MALL_ORDER_STATUS[v] || '';
  }

  async function load() {
    try {
      const [detailRes, configRes] = await Promise.all([
        mallH5Api.orderDetail(orderId.value),
        mallH5Api.config().catch(() => ({ data: {} })),
      ]);
      order.value = detailRes.data || {};
      config.value = configRes.data || {};
      if (order.value.payNote) {
        payNote.value = order.value.payNote;
      }
      if (order.value.payProofUrl) {
        payProofUrl.value = order.value.payProofUrl;
      }
    } catch (e) {
      // toast already shown
    }
  }

  function chooseProof() {
    if (uploading.value || order.value.orderStatus !== 10) {
      return;
    }
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const path = res.tempFilePaths && res.tempFilePaths[0];
        if (!path) {
          return;
        }
        uploading.value = true;
        try {
          const up = await mallH5Api.uploadAvatar(path);
          payProofUrl.value = (up.data && (up.data.fileUrl || up.data.fileKey)) || '';
        } catch (e) {
          payProofUrl.value = '';
        } finally {
          uploading.value = false;
        }
      },
    });
  }

  async function submitProof() {
    if (!hasPayQr.value) {
      SmartToast.toast('商家尚未配置收款码');
      return;
    }
    if (!payProofUrl.value) {
      SmartToast.toast('请上传付款截图');
      return;
    }
    submitting.value = true;
    try {
      await mallH5Api.submitPayProof({
        orderId: order.value.orderId,
        payProofUrl: payProofUrl.value,
        payNote: payNote.value,
      });
      SmartToast.success('已提交，等待商家确认');
      await load();
    } catch (e) {
      // toast already shown
    } finally {
      submitting.value = false;
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
  });

  onShow(() => {
    if (orderId.value) {
      load();
    }
  });
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
    padding-bottom: 160rpx;
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
  .row {
    color: #888;
    font-size: 24rpx;
    margin-bottom: 12rpx;
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
  .sub {
    font-size: 28rpx;
    font-weight: 600;
    margin-bottom: 20rpx;
  }
  .warn {
    color: #ee0a24;
    font-size: 24rpx;
    margin-bottom: 16rpx;
  }
  .qr-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 32rpx;
  }
  .qr-item {
    width: 48%;
    text-align: center;
  }
  .qr,
  .proof {
    width: 100%;
    height: 280rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
  }
  .proof.wide {
    height: 360rpx;
  }
  .qr.empty,
  .proof.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #bbb;
    font-size: 26rpx;
    border: 2rpx dashed #ddd;
    box-sizing: border-box;
  }
  .qr-tip {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #666;
  }
  .upload {
    margin-bottom: 20rpx;
  }
  .note {
    width: 100%;
    min-height: 140rpx;
    background: #f7f7f7;
    border-radius: 12rpx;
    padding: 16rpx;
    box-sizing: border-box;
    font-size: 26rpx;
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
  .buy[disabled] {
    opacity: 0.6;
  }
</style>
