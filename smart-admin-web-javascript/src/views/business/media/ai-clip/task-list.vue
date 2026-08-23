<template>
  <a-card size="small" :bordered="false" title="成片任务">
    <a-button type="primary" class="mb" @click="visible = true">提交成片任务</a-button>
    <a-spin :spinning="loading">
      <a-table :dataSource="list" :columns="columns" rowKey="taskId" :pagination="false" size="small">
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag>{{ $smartEnumPlugin.getDescByValue('AI_CLIP_TASK_STATUS_ENUM', record.status) }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'progress'">
            <a-progress :percent="record.progress" size="small" />
          </template>
        </template>
      </a-table>
    </a-spin>
    <a-modal v-model:open="visible" title="一键成片" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="成片方式">
          <a-select v-model:value="form.type" :options="['一键成片', '口播成片', '图文成片', '踩点成片'].map((v) => ({ label: v, value: v }))" />
        </a-form-item>
        <a-form-item label="作品名称"><a-input v-model:value="form.title" /></a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const list = ref([]);
  const loading = ref(false);
  const visible = ref(false);
  const form = reactive({ type: '一键成片', title: '新的成片任务' });
  const columns = [
    { title: '任务', dataIndex: 'title' },
    { title: '方式', dataIndex: 'type', width: 120 },
    { title: '进度', dataIndex: 'progress', width: 180 },
    { title: '状态', dataIndex: 'status', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
  ];

  async function load() {
    loading.value = true;
    try {
      const res = await aiClipApi.queryTask({});
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function submit() {
    try {
      await aiClipApi.createTask({ title: form.title, type: form.type });
      visible.value = false;
      message.success('已提交到云端成片队列');
      await load();
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  onMounted(load);
</script>

<style scoped>
  .mb {
    margin-bottom: 12px;
  }
</style>
