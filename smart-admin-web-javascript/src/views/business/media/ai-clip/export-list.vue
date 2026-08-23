<template>
  <a-card size="small" :bordered="false" title="导出 / 发布">
    <a-alert type="info" show-icon message="成片完成后可下载云端成片或发布。未完成任务不可下载。" class="smart-margin-bottom10" />
    <a-spin :spinning="loading">
      <a-table :dataSource="list" :columns="columns" rowKey="taskId" :pagination="false" size="small">
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag>{{ $smartEnumPlugin.getDescByValue('AI_CLIP_TASK_STATUS_ENUM', record.status) }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" :disabled="record.status !== 30 || !record.resultUrl" @click="openResult(record)">下载成片</a-button>
              <a-button type="link" :disabled="record.status !== 30">发布</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-spin>
  </a-card>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const list = ref([]);
  const loading = ref(false);
  const columns = [
    { title: '作品', dataIndex: 'title' },
    { title: '方式', dataIndex: 'type', width: 120 },
    { title: '状态', dataIndex: 'status', width: 100 },
    { title: '时间', dataIndex: 'createTime', width: 170 },
    { title: '操作', dataIndex: 'action', width: 180 },
  ];

  function openResult(record) {
    if (record.resultUrl) {
      window.open(record.resultUrl, '_blank');
    }
  }

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await aiClipApi.queryTask({});
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>
