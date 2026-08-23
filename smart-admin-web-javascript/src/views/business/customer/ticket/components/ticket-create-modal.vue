<!--
  * 创建工单弹窗
-->
<template>
  <a-modal
    :title="'创建工单'"
    :open="visible"
    :confirmLoading="confirmLoading"
    @ok="onSubmit"
    @cancel="onClose"
    width="600px"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :labelCol="{ span: 5 }" :wrapperCol="{ span: 18 }">
      <a-form-item label="工单标题" name="title">
        <a-input v-model:value="form.title" placeholder="请输入工单标题" :maxlength="200" />
      </a-form-item>
      <a-form-item label="工单类型" name="ticketType">
        <SmartEnumSelect enum-name="TICKET_TYPE_ENUM" v-model:value="form.ticketType" width="100%" />
      </a-form-item>
      <a-form-item label="优先级" name="priority">
        <SmartEnumSelect enum-name="TICKET_PRIORITY_ENUM" v-model:value="form.priority" width="100%" />
      </a-form-item>
      <a-form-item label="工单内容" name="content">
        <a-textarea v-model:value="form.content" placeholder="请详细描述您的问题" :rows="5" :maxlength="2000" showCount />
      </a-form-item>
      <a-form-item label="联系人" name="contactName">
        <a-input v-model:value="form.contactName" placeholder="请输入联系人" :maxlength="50" />
      </a-form-item>
      <a-form-item label="联系电话" name="contactPhone">
        <a-input v-model:value="form.contactPhone" placeholder="请输入联系电话" :maxlength="20" />
      </a-form-item>
      <a-form-item label="联系邮箱" name="contactEmail">
        <a-input v-model:value="form.contactEmail" placeholder="请输入联系邮箱" :maxlength="100" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import { ticketApi } from '/@/api/business/customer/ticket-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

  const emit = defineEmits(['reloadList']);

  const formRef = ref();
  const visible = ref(false);
  const confirmLoading = ref(false);

  const defaultForm = {
    title: '',
    ticketType: 1,
    priority: 2,
    content: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
  };
  const form = ref({ ...defaultForm });

  const rules = {
    title: [{ required: true, message: '请输入工单标题' }],
    ticketType: [{ required: true, message: '请选择工单类型' }],
    priority: [{ required: true, message: '请选择优先级' }],
    content: [{ required: true, message: '请输入工单内容' }],
  };

  function showModal() {
    form.value = { ...defaultForm };
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
  }

  async function onSubmit() {
    try {
      await formRef.value.validate();
    } catch {
      return;
    }
    confirmLoading.value = true;
    try {
      SmartLoading.show();
      await ticketApi.create(form.value);
      message.success('工单创建成功');
      visible.value = false;
      emit('reloadList');
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
      confirmLoading.value = false;
    }
  }

  defineExpose({ showModal });
</script>