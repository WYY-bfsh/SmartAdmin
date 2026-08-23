<template>
  <a-card size="small" :bordered="false" title="素材库">
    <template #extra>
      <a-button type="primary" @click="uploadRef?.showModal()">上传素材</a-button>
    </template>
    <a-tabs v-model:activeKey="type" @change="load">
      <a-tab-pane :key="0" tab="全部" />
      <a-tab-pane v-for="item in types" :key="item.value" :tab="item.desc" />
    </a-tabs>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="素材库为空，请联网后上传视频 / 图片 / 音频" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="6" v-for="item in list" :key="item.materialId">
          <a-card hoverable size="small">
            <img :src="item.coverUrl" class="cover" alt="" />
            <div class="name">{{ item.name }}</div>
            <div class="meta">{{ item.duration }} · {{ item.size }}</div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
    <MaterialUploadModal ref="uploadRef" @ok="load" />
  </a-card>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { AI_CLIP_MATERIAL_TYPE_ENUM } from '/@/constants/business/media/ai-clip-const';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import MaterialUploadModal from './components/material-upload-modal.vue';

  const type = ref(0);
  const types = Object.values(AI_CLIP_MATERIAL_TYPE_ENUM);
  const list = ref([]);
  const loading = ref(false);
  const uploadRef = ref();

  async function load() {
    loading.value = true;
    try {
      const res = await aiClipApi.queryMaterial({ type: type.value || undefined });
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }

  onMounted(load);
</script>

<style scoped>
  .cover {
    width: 100%;
    height: 120px;
    object-fit: cover;
    border-radius: 4px;
  }
  .name {
    margin-top: 8px;
    font-weight: 600;
  }
  .meta {
    color: #888;
    font-size: 12px;
  }
</style>
