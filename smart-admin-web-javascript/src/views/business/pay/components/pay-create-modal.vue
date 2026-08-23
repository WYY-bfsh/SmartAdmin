<!--
  * 发起微信支付
-->
<template>
  <a-modal :open="visible" title="发起微信支付" :width="480" ok-text="生成收款码" cancel-text="取消" @ok="onSubmit" @cancel="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 6 }">
      <a-form-item label="商品描述" name="description">
        <a-input v-model:value="form.description" placeholder="用户扫码时看到的商品名称" />
      </a-form-item>
      <a-form-item label="支付金额" name="amountYuan">
        <a-input-number v-model:value="form.amountYuan" :min="0.01" :precision="2" :step="1" style="width: 100%" placeholder="单位：元" />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-textarea v-model:value="form.remark" :rows="3" placeholder="可选" />
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

  const emit = defineEmits(['reloadList', 'showQrcode']);

  const formRef = ref();
  const visible = ref(false);
  const formDefault = {
    description: '',
    amountYuan: undefined,
    remark: '',
  };
  const form = reactive({ ...formDefault });
  const rules = {
    description: [{ required: true, message: '请输入商品描述' }],
    amountYuan: [{ required: true, message: '请输入支付金额' }],
  };

  function showModal() {
    Object.assign(form, formDefault);
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
          const res = await payApi.create(form);
          message.success('已生成收款码，请使用微信扫码支付');
          visible.value = false;
          emit('reloadList');
          emit('showQrcode', res.data);
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
