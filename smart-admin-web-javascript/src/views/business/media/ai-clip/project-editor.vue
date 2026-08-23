<template>
  <div class="editor">
    <a-spin :spinning="loading">
      <div class="top">
        <div>
          <a-button @click="$router.back()">返回</a-button>
          <b class="title">{{ project.title || '加载中' }}</b>
          <a-tag v-if="project.ratio">{{ project.ratio }}</a-tag>
        </div>
        <a-space>
          <a-button>智能字幕</a-button>
          <a-button>卡点匹配</a-button>
          <a-button type="primary" @click="$router.push('/media/ai-clip/task')">导出成片</a-button>
        </a-space>
      </div>
      <div class="stage">
        <div class="preview">
          <video v-if="previewUrl" :src="previewUrl" controls class="player" />
          <img v-else-if="project.coverUrl" :src="project.coverUrl" alt="" />
          <div>预览窗口 · 素材来自云端素材库</div>
        </div>
        <div class="side">
          <h4>素材箱</h4>
          <a-empty v-if="!materials.length" description="请先上传素材" />
          <div class="mat" v-for="item in materials" :key="item.materialId" @click="previewUrl = item.fileUrl || item.coverUrl">
            <img :src="item.coverUrl" alt="" />
            <span>{{ item.name }}</span>
          </div>
        </div>
      </div>
      <div class="timeline">
        <div class="track" v-for="track in tracks" :key="track.name">
          <div class="label">{{ track.name }}</div>
          <div class="clips">
            <div v-for="clip in track.clips" :key="clip" class="clip" :style="{ width: clip }">{{ track.name }}</div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const route = useRoute();
  const loading = ref(false);
  const project = ref({});
  const materials = ref([]);
  const previewUrl = ref('');
  const tracks = [
    { name: '视频', clips: ['32%', '22%', '18%'] },
    { name: '音频', clips: ['70%'] },
    { name: '字幕', clips: ['18%', '20%', '16%'] },
  ];

  onMounted(async () => {
    loading.value = true;
    try {
      const [detail, materialRes] = await Promise.all([
        aiClipApi.projectDetail(route.params.id),
        aiClipApi.queryMaterial({}),
      ]);
      project.value = detail.data || {};
      materials.value = unwrapList(materialRes.data).slice(0, 8);
      previewUrl.value = materials.value.find((e) => e.fileUrl)?.fileUrl || '';
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>

<style scoped>
  .editor {
    background: #141414;
    color: #eee;
    min-height: calc(100vh - 120px);
    padding: 12px;
    border-radius: 8px;
  }
  .top {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  .title {
    margin: 0 12px;
    font-size: 16px;
  }
  .stage {
    display: grid;
    grid-template-columns: 1fr 260px;
    gap: 12px;
  }
  .preview {
    background: #000;
    min-height: 320px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
  }
  .preview img,
  .player {
    max-height: 260px;
    max-width: 100%;
    border-radius: 4px;
  }
  .side {
    background: #1f1f1f;
    border-radius: 8px;
    padding: 12px;
  }
  .mat {
    display: flex;
    gap: 8px;
    align-items: center;
    margin-bottom: 8px;
    cursor: pointer;
  }
  .mat img {
    width: 48px;
    height: 32px;
    object-fit: cover;
    border-radius: 4px;
  }
  .timeline {
    margin-top: 12px;
    background: #1a1a1a;
    padding: 12px;
    border-radius: 8px;
  }
  .track {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }
  .label {
    width: 48px;
    color: #999;
  }
  .clips {
    flex: 1;
    display: flex;
    gap: 6px;
    background: #111;
    padding: 8px;
    border-radius: 4px;
  }
  .clip {
    background: linear-gradient(90deg, #722ed1, #eb2f96);
    height: 28px;
    border-radius: 4px;
    font-size: 12px;
    display: flex;
    align-items: center;
    padding: 0 8px;
  }
</style>
