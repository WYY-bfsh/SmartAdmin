<template>
  <div class="studio">
    <div class="hero">
      <div>
        <h2>AI 漫剪工作台</h2>
        <p>素材、成片、模板全部走云端，需保持网络连接</p>
      </div>
      <a-space>
        <a-button type="primary" @click="$router.push('/media/ai-clip/task')">一键成片</a-button>
        <a-button @click="$router.push('/media/ai-clip/script')">口播脚本</a-button>
        <a-button @click="$router.push('/media/ai-clip/template')">用模板</a-button>
      </a-space>
    </div>
    <a-spin :spinning="loading">
      <a-row :gutter="16">
        <a-col :span="6" v-for="item in stats" :key="item.label">
          <a-card size="small">
            <a-statistic :title="item.label" :value="item.value" />
          </a-card>
        </a-col>
      </a-row>
      <a-card class="block" title="最近作品" size="small">
        <a-empty v-if="!workspace.recentProjects?.length" description="暂无作品，先去创建或上传素材" />
        <a-row :gutter="16" v-else>
          <a-col :span="8" v-for="item in workspace.recentProjects" :key="item.projectId">
            <div class="proj" @click="$router.push(`/media/ai-clip/project/editor/${item.projectId}`)">
              <img :src="item.coverUrl" alt="" />
              <div>
                <b>{{ item.title }}</b>
                <div>{{ item.duration }} · {{ statusText(item.status) }}</div>
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { AI_CLIP_PROJECT_STATUS_ENUM } from '/@/constants/business/media/ai-clip-const';
  import { loadOnline } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const loading = ref(false);
  const workspace = reactive({
    projectCount: 0,
    materialCount: 0,
    templateCount: 0,
    taskToday: 0,
    recentProjects: [],
  });
  const stats = computed(() => [
    { label: '作品', value: workspace.projectCount },
    { label: '素材', value: workspace.materialCount },
    { label: '模板', value: workspace.templateCount },
    { label: '今日任务', value: workspace.taskToday },
  ]);

  function statusText(v) {
    return Object.values(AI_CLIP_PROJECT_STATUS_ENUM).find((e) => e.value === v)?.desc || '-';
  }

  onMounted(async () => {
    loading.value = true;
    try {
      Object.assign(workspace, await loadOnline(() => aiClipApi.workspace()));
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>

<style scoped>
  .hero {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    margin-bottom: 16px;
    border-radius: 10px;
    color: #fff;
    background: linear-gradient(120deg, #111 0%, #3d1b5d 55%, #ff4d4f 140%);
  }
  .hero p {
    opacity: 0.8;
    margin: 4px 0 0;
  }
  .block {
    margin-top: 16px;
  }
  .proj {
    display: flex;
    gap: 12px;
    cursor: pointer;
  }
  .proj img {
    width: 120px;
    height: 68px;
    object-fit: cover;
    border-radius: 6px;
  }
</style>
