<template>
  <!-- 订单详情：待付款展示商家收款码并上传凭证；待确认等待商家审核 -->
  <view class="page">
    <view class="card" v-if="order.orderId">
      <view class="st">{{ statusText(order.orderStatus) }}</view>
      <view class="row">订单号 {{ order.orderNo }}</view>
      <view>{{ order.goodsName }} × {{ order.qty }}</view>
      <view class="price">应付 ¥{{ order.amount }}</view>
      <view v-if="order.payChannel === 20" class="addr">微信支付 {{ order.wxTransactionId || '' }}</view>
      <view v-if="order.payChannel === 30" class="addr">支付宝 {{ order.wxTransactionId || '' }}</view>
      <view class="addr">{{ order.receiverName }} {{ order.receiverPhone }}\n{{ order.receiverAddress }}</view>
      <view v-if="order.waybillNo" class="logi" @click="goExpress">
        {{ order.expressName }} {{ order.waybillNo }} · 查看物流
      </view>
    </view>

    <view class="card" v-if="order.orderStatus === 10 && alipayEnabled">
      <view class="sub">支付宝支付</view>
      <view class="addr" v-if="config.alipayPayMock">当前是演示模式，不会向支付宝收款。</view>
      <view v-if="aliQr" class="wx-box">
        <image class="qr wx" :src="aliQr" mode="aspectFit" />
        <view class="qr-tip">请使用支付宝扫码支付</view>
      </view>
      <button class="buy" :disabled="paying" @click="startAlipayPay">{{ paying ? '支付处理中…' : '支付宝付款' }}</button>
      <button v-if="aliPayOrderId" class="ghost" @click="openAlipayWap">手机打开支付宝收银台</button>
      <button v-if="config.alipayPayMock" class="ghost" :disabled="paying" @click="mockAlipayPay">演示：模拟支付宝支付成功</button>
    </view>

    <view class="card" v-if="order.orderStatus === 10 && wechatEnabled">
      <view class="sub">微信支付</view>
      <view class="addr" v-if="config.wechatPayMock">当前是演示模式，不会向微信收款。</view>
      <view v-if="wxQr" class="wx-box">
        <image class="qr wx" :src="wxQr" mode="aspectFit" />
        <view class="qr-tip">请使用微信扫码支付</view>
      </view>
      <view v-if="paying" class="addr">正在处理支付，支付完成后本页会自动更新</view>
      <button class="buy" :disabled="paying" @click="startWechatPay">{{ wechatBtnText }}</button>
      <button v-if="config.wechatPayMock" class="ghost" :disabled="paying" @click="mockWechatPay">演示：模拟支付成功</button>
    </view>

    <view class="card" v-if="order.orderStatus === 10">
      <view class="sub">{{ wechatEnabled ? '或扫商家收款码转账' : '请扫商家收款码转账' }}</view>
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
  import { computed, onUnmounted, ref } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import { mallH5Api, MALL_ORDER_STATUS, resolveMallFileUrl } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const orderId = ref('');
  const wxOauthCode = ref('');
  const order = ref({});
  const config = ref({});
  const payProofUrl = ref('');
  const payNote = ref('');
  const uploading = ref(false);
  const submitting = ref(false);
  const paying = ref(false);
  const wxQr = ref('');
  const aliQr = ref('');
  const aliPayOrderId = ref('');
  const openid = ref('');
  let pollTimer = null;
  let autoStarted = false;
  let autoAliStarted = false;

  const wechatQr = computed(() => resolveMallFileUrl(config.value.merchantWechatQr));
  const alipayQr = computed(() => resolveMallFileUrl(config.value.merchantAlipayQr));
  const hasPayQr = computed(() => !!(wechatQr.value || alipayQr.value));
  const proofPreview = computed(() => resolveMallFileUrl(payProofUrl.value || order.value.payProofUrl));
  const wechatEnabled = computed(() => !!config.value.wechatPayEnabled);
  const alipayEnabled = computed(() => !!config.value.alipayPayEnabled);
  const wechatBtnText = computed(() => {
    if (paying.value) {
      return '支付处理中…';
    }
    if (isWeixin()) {
      return '微信付款';
    }
    if (isMobile()) {
      return '手机微信支付';
    }
    return wxQr.value ? '刷新收款码' : '获取微信收款码';
  });

  function statusText(v) {
    return MALL_ORDER_STATUS[v] || '';
  }

  function isWeixin() {
    // #ifdef H5
    return typeof navigator !== 'undefined' && /MicroMessenger/i.test(navigator.userAgent);
    // #endif
    return false;
  }

  function isMobile() {
    // #ifdef H5
    return typeof navigator !== 'undefined' && /Android|iPhone|iPad|iPod/i.test(navigator.userAgent);
    // #endif
    return false;
  }

  function stopPoll() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
  }

  function startPoll() {
    stopPoll();
    pollTimer = setInterval(async () => {
      try {
        const res = await mallH5Api.orderDetail(orderId.value);
        order.value = res.data || {};
        if (order.value.orderStatus && order.value.orderStatus !== 10) {
          stopPoll();
          paying.value = false;
          if (order.value.orderStatus === 20 || order.value.orderStatus === 15) {
            SmartToast.success(order.value.orderStatus === 20 ? '支付成功' : '已提交');
          }
        }
      } catch (e) {
        // toast already shown
      }
    }, 2000);
  }

  function invokeJsapi(pay) {
    return new Promise((resolve, reject) => {
      // #ifdef H5
      const run = () => {
        if (typeof WeixinJSBridge === 'undefined') {
          reject(new Error('请在微信内打开'));
          return;
        }
        WeixinJSBridge.invoke(
          'getBrandWCPayRequest',
          {
            appId: pay.jsapiAppId,
            timeStamp: pay.jsapiTimeStamp,
            nonceStr: pay.jsapiNonceStr,
            package: pay.jsapiPackage,
            signType: pay.jsapiSignType,
            paySign: pay.jsapiPaySign,
          },
          (res) => {
            if (res.err_msg === 'get_brand_wcpay_request:ok') {
              resolve();
              return;
            }
            if (res.err_msg === 'get_brand_wcpay_request:cancel') {
              reject(new Error('已取消支付'));
              return;
            }
            reject(new Error(res.err_msg || '支付失败'));
          }
        );
      };
      if (typeof WeixinJSBridge === 'undefined') {
        document.addEventListener('WeixinJSBridgeReady', run, { once: true });
      } else {
        run();
      }
      return;
      // #endif
      reject(new Error('请使用 H5 在微信中支付'));
    });
  }

  async function startWechatPay() {
    if (!wechatEnabled.value || order.value.orderStatus !== 10) {
      return;
    }
    paying.value = true;
    try {
      if (isWeixin() && config.value.wechatJsapiReady) {
        if (!openid.value && wxOauthCode.value) {
          const auth = await mallH5Api.wechatOauth(wxOauthCode.value);
          openid.value = auth.data || '';
          wxOauthCode.value = '';
        }
        if (!openid.value) {
          // #ifdef H5
          const redirectUri = `${location.origin}${location.pathname}`;
          const urlRes = await mallH5Api.wechatOauthUrl(redirectUri, `mallpay_${orderId.value}`);
          if (urlRes.data) {
            location.href = urlRes.data;
            return;
          }
          // #endif
          SmartToast.toast('无法发起微信授权');
          paying.value = false;
          return;
        }
        const pre = await mallH5Api.wechatPrepay({
          orderId: order.value.orderId,
          tradeType: 'jsapi',
          openid: openid.value,
        });
        const data = pre.data || {};
        if (data.needOpenid) {
          SmartToast.toast('请先完成微信授权');
          paying.value = false;
          return;
        }
        if (data.mock) {
          startPoll();
          return;
        }
        await invokeJsapi(data);
        startPoll();
        return;
      }
      if (isMobile() && !isWeixin()) {
        const pre = await mallH5Api.wechatPrepay({
          orderId: order.value.orderId,
          tradeType: 'h5',
        });
        startPoll();
        if (pre.data && pre.data.h5Url) {
          // #ifdef H5
          location.href = pre.data.h5Url;
          // #endif
        }
        return;
      }
      const pre = await mallH5Api.wechatPrepay({
        orderId: order.value.orderId,
        tradeType: 'native',
      });
      wxQr.value = (pre.data && pre.data.qrcodeBase64) || '';
      startPoll();
    } catch (e) {
      paying.value = false;
    }
  }

  async function startAlipayPay() {
    if (!alipayEnabled.value || order.value.orderStatus !== 10) {
      return;
    }
    paying.value = true;
    try {
      const pre = await mallH5Api.alipayPrepay({
        orderId: order.value.orderId,
        tradeType: 'native',
      });
      aliQr.value = (pre.data && pre.data.qrcodeBase64) || '';
      aliPayOrderId.value = (pre.data && pre.data.payOrderId) || '';
      startPoll();
    } catch (e) {
      paying.value = false;
    }
  }

  function openAlipayWap() {
    if (!aliPayOrderId.value || typeof window === 'undefined') {
      return;
    }
    window.location.href = `${window.location.origin}/api/pay/alipay/wap/${aliPayOrderId.value}`;
  }

  async function mockAlipayPay() {
    paying.value = true;
    try {
      await mallH5Api.alipayMockPay(order.value.orderId);
      SmartToast.success('已模拟支付宝支付');
      await load();
    } catch (e) {
      // toast already shown
    } finally {
      paying.value = false;
    }
  }

  async function mockWechatPay() {
    paying.value = true;
    try {
      await mallH5Api.wechatMockPay(order.value.orderId);
      SmartToast.success('已模拟支付');
      await load();
    } catch (e) {
      // toast already shown
    } finally {
      paying.value = false;
    }
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
      if (order.value.orderStatus === 10 && wechatEnabled.value && !autoStarted) {
        autoStarted = true;
        if (wxOauthCode.value || (!isWeixin() && !isMobile())) {
          startWechatPay();
        }
      }
      if (order.value.orderStatus === 10 && alipayEnabled.value && !autoAliStarted) {
        autoAliStarted = true;
        startAlipayPay();
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
    wxOauthCode.value = options.wxcode || '';
  });

  onShow(() => {
    if (orderId.value) {
      load();
    }
  });

  onUnmounted(() => {
    stopPoll();
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
  .ghost {
    margin-top: 16rpx;
    background: #fff;
    color: #ee0a24;
    border: 2rpx solid #ee0a24;
    border-radius: 48rpx;
  }
  .wx-box {
    text-align: center;
    margin-bottom: 24rpx;
  }
  .qr.wx {
    width: 420rpx;
    height: 420rpx;
    margin: 0 auto;
  }
</style>
