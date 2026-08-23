<!--
  * 知识库表单弹窗
-->
<template>
  <a-modal
    :title="isEdit ? '编辑知识' : '新增知识'"
    :open="visible"
    :confirmLoading="confirmLoading"
    @ok="onSubmit"
    @cancel="onClose"
    width="700px"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :labelCol="{ span: 4 }" :wrapperCol="{ span: 19 }">
      <a-form-item label="标题" name="title">
        <a-input v-model:value="form.title" placeholder="请输入标题" :maxlength="200" />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <SmartEnumSelect enum-name="KNOWLEDGE_CATEGORY_ENUM" v-model:value="form.category" width="100%" />
      </a-form-item>
      <a-form-item label="排序" name="sort">
        <a-input-number v-model:value="form.sort" :min="0" :max="9999" style="width: 100%" />
      </a-form-item>
      <a-form-item label="内容" name="content">
        <WangEditor v-model="form.content" height="300px" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { knowledgeApi } from '/@/api/business/customer/knowledge-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import WangEditor from '/@/components/framework/wangeditor/index.vue';

  const emit = defineEmits(['reloadList']);

  const formRef = ref();
  const visible = ref(false);
  const confirmLoading = ref(false);
  const isEdit = ref(false);
  const editId = ref(null);

  const defaultForm = {
    title: '',
    category: 1,
    sort: 0,
    content: '',
  };
  const form = ref({ ...defaultForm });

  const rules = {
    title: [{ required: true, message: '请输入标题' }],
    category: [{ required: true, message: '请选择分类' }],
  };

  async function showModal(knowledgeId) {
    form.value = { ...defaultForm };
    if (knowledgeId) {
      isEdit.value = true;
      editId.value = knowledgeId;
      try {
        SmartLoading.show();
        const res = await knowledgeApi.detail(knowledgeId);
        form.value = {
          title: res.data.title || '',
          category: res.data.category || 1,
          sort: res.data.sort || 0,
          content: res.data.content || '',
        };
      } catch (e) {
        smartSentry.captureError(e);
      } finally {
        SmartLoading.hide();
      }
    } else {
      isEdit.value = false;
      editId.value = null;
    }
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
      if (isEdit.value) {
        await knowledgeApi.update({ knowledgeId: editId.value, ...form.value });
        message.success('更新成功');
      } else {
        await knowledgeApi.add(form.value);
        message.success('新增成功');
      }
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