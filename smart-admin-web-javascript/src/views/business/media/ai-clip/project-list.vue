<template>
  <a-card size="small" :bordered="false">
    <div class="toolbar">
      <a-input-search v-model:value="keyword" placeholder="搜索作品" style="width: 240px" @search="load" />
      <SmartEnumSelect enum-name="AI_CLIP_PROJECT_STATUS_ENUM" v-model:value="status" width="140px" />
      <a-button type="primary" @click="createVisible = true">新建草稿</a-button>
    </div>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="没有作品，请保持联网后新建" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="6" v-for="item in list" :key="item.projectId">
          <div class="card" @click="$router.push(`/media/ai-clip/project/editor/${item.projectId}`)">
            <img :src="item.coverUrl" alt="" />
            <div class="body">
              <b>{{ item.title }}</b>
              <div>{{ item.ratio }} · {{ item.duration }}</div>
              <a-tag>{{ $smartEnumPlugin.getDescByValue('AI_CLIP_PROJECT_STATUS_ENUM', item.status) }}</a-tag>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-spin>
    <a-modal v-model:open="createVisible" title="新建草稿" @ok="onCreate">
      <a-input v-model:value="newTitle" placeholder="作品名称，如：夏日漫剪" />
    </a-modal>
  </a-card>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const router = useRouter();
  const keyword = ref('');
  const status = ref();
  const list = ref([]);
  const loading = ref(false);
  const createVisible = ref(false);
  const newTitle = ref('');

  async function load() {
    loading.value = true;
    try {
      const res = await aiClipApi.queryProject({ keyword: keyword.value, status: status.value });
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function onCreate() {
    try {
      const res = await aiClipApi.saveProject({ title: newTitle.value || '未命名作品', status: 10, ratio: '9:16' });
      createVisible.value = false;
      message.success('草稿已保存到云端');
      router.push(`/media/ai-clip/project/editor/${res.data}`);
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  onMounted(load);
</script>

<style scoped>
  .toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
  .card {
    cursor: pointer;
    border-radius: 8px;
    overflow: hidden;
    background: #fafafa;
  }
  .card img {
    width: 100%;
    height: 140px;
    object-fit: cover;
  }
  .body {
    padding: 10px;
  }
</style>
