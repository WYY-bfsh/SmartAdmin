<!--
  * 微信支付二维码
-->
<template>
  <a-modal :open="visible" title="微信扫码支付" :footer="null" :width="420" @cancel="onClose">
    <div class="pay-qrcode">
      <div class="pay-qrcode-desc">{{ payInfo.description }}</div>
      <div class="pay-qrcode-amount">¥{{ formatMoney(payInfo.amountYuan) }}</div>
      <img v-if="payInfo.qrcodeBase64" class="pay-qrcode-img" :src="payInfo.qrcodeBase64" alt="微信支付二维码" />
      <a-spin v-else />
      <a-tag :color="payInfo.payStatus === PAY_STATUS_ENUM.SUCCESS.value ? 'green' : 'orange'">
        {{ $smartEnumPlugin.getDescByValue('PAY_STATUS_ENUM', payInfo.payStatus) }}
      </a-tag>
      <div class="pay-qrcode-tip">请使用微信扫描二维码完成支付，支付成功后会自动刷新</div>
      <div class="pay-qrcode-no">订单号：{{ payInfo.orderNo }}</div>
    </div>
  </a-modal>
</template>

<script setup>
  import { onBeforeUnmount, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { PAY_STATUS_ENUM } from '/@/constants/business/pay/pay-const';
  import { smartSentry } from '/@/lib/smart-sentry';

  const emit = defineEmits(['paid']);

  const visible = ref(false);
  const payInfo = reactive({
    payOrderId: undefined,
    orderNo: '',
    description: '',
    amountYuan: 0,
    qrcodeBase64: '',
    payStatus: PAY_STATUS_ENUM.WAIT_PAY.value,
  });

  let timer = null;

  function formatMoney(value) {
    if (value === undefined || value === null || value === '') {
      return '0.00';
    }
    return Number(value).toFixed(2);
  }

  async function showModal(payOrderIdOrData) {
    stopPoll();
    Object.assign(payInfo, {
      payOrderId: undefined,
      orderNo: '',
      description: '',
      amountYuan: 0,
      qrcodeBase64: '',
      payStatus: PAY_STATUS_ENUM.WAIT_PAY.value,
    });
    visible.value = true;
    try {
      if (typeof payOrderIdOrData === 'object') {
        Object.assign(payInfo, payOrderIdOrData);
      } else {
        const res = await payApi.qrcode(payOrderIdOrData);
        Object.assign(payInfo, res.data);
      }
      startPoll();
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  function startPoll() {
    stopPoll();
    timer = setInterval(async () => {
      if (!payInfo.payOrderId) {
        return;
      }
      try {
        const res = await payApi.detail(payInfo.payOrderId);
        payInfo.payStatus = res.data.payStatus;
        if (res.data.payStatus === PAY_STATUS_ENUM.SUCCESS.value) {
          stopPoll();
          message.success('支付成功');
          emit('paid');
          setTimeout(onClose, 800);
        }
        if (res.data.payStatus === PAY_STATUS_ENUM.CLOSED.value) {
          stopPoll();
          message.warning('订单已关闭');
        }
      } catch (e) {
        smartSentry.captureError(e);
      }
    }, 2500);
  }

  function stopPoll() {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  function onClose() {
    stopPoll();
    visible.value = false;
  }

  onBeforeUnmount(stopPoll);

  defineExpose({ showModal });
</script>

<style scoped>
  .pay-qrcode {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px 0 16px;
  }

  .pay-qrcode-desc {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .pay-qrcode-amount {
    font-size: 28px;
    font-weight: 600;
    color: #1677ff;
    margin-bottom: 16px;
  }

  .pay-qrcode-img {
    width: 240px;
    height: 240px;
    margin-bottom: 16px;
  }

  .pay-qrcode-tip {
    color: #999;
    margin-top: 12px;
    text-align: center;
  }

  .pay-qrcode-no {
    color: #666;
    margin-top: 8px;
    font-size: 12px;
  }
</style>
