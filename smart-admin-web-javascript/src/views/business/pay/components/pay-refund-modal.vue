<!--
  * 申请退款
-->
<template>
  <a-modal :open="visible" title="申请退款" :width="480" ok-text="确认退款" cancel-text="取消" @ok="onSubmit" @cancel="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 6 }">
      <a-form-item label="商户订单号">
        <span>{{ order.orderNo }}</span>
      </a-form-item>
      <a-form-item label="订单金额">
        <span>¥{{ formatMoney(order.amountYuan) }}</span>
      </a-form-item>
      <a-form-item label="已退款">
        <span>¥{{ formatMoney(order.refundAmountYuan || 0) }}</span>
      </a-form-item>
      <a-form-item label="退款金额" name="refundAmountYuan">
        <a-input-number v-model:value="form.refundAmountYuan" :min="0.01" :precision="2" :step="1" style="width: 100%" placeholder="单位：元" />
      </a-form-item>
      <a-form-item label="退款原因" name="reason">
        <a-textarea v-model:value="form.reason" :rows="3" placeholder="可选" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  const emit = defineEmits(['reloadList']);
  const formRef = ref();
  const visible = ref(false);
  const order = reactive({
    payOrderId: undefined,
    orderNo: '',
    amountYuan: 0,
    refundAmountYuan: 0,
  });
  const form = reactive({
    payOrderId: undefined,
    refundAmountYuan: undefined,
    reason: '',
  });
  const rules = {
    refundAmountYuan: [{ required: true, message: '请输入退款金额' }],
  };

  function formatMoney(value) {
    if (value === undefined || value === null || value === '') {
      return '0.00';
    }
    return Number(value).toFixed(2);
  }

  function showModal(record) {
    Object.assign(order, record);
    form.payOrderId = record.payOrderId;
    const remain = Number(record.amountYuan || 0) - Number(record.refundAmountYuan || 0);
    form.refundAmountYuan = Number(remain.toFixed(2));
    form.reason = '';
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
  }

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        SmartLoading.show();
        try {
          await payApi.refund(form);
          message.success('退款申请已提交');
          visible.value = false;
          emit('reloadList');
        } catch (e) {
          smartSentry.captureError(e);
        } finally {
          SmartLoading.hide();
        }
      })
      .catch(() => {});
  }

  defineExpose({ showModal });
</script>
