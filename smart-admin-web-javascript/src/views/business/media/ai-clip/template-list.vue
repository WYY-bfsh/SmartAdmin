<template>
  <a-card size="small" :bordered="false" title="模板中心">
    <a-radio-group v-model:value="scene" button-style="solid" @change="load" style="margin-bottom: 16px">
      <a-radio-button :value="0">全部</a-radio-button>
      <a-radio-button v-for="item in scenes" :key="item.value" :value="item.value">{{ item.desc }}</a-radio-button>
    </a-radio-group>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="模板需从云端拉取，请检查网络" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="6" v-for="item in list" :key="item.templateId">
          <div class="tpl" @click="$router.push(`/media/ai-clip/template/${item.templateId}`)">
            <img :src="item.coverUrl" alt="" />
            <b>{{ item.name }}</b>
            <div>使用 {{ item.useCount }} · {{ item.duration }}</div>
          </div>
        </a-col>
      </a-row>
    </a-spin>
  </a-card>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { AI_CLIP_TEMPLATE_SCENE_ENUM } from '/@/constants/business/media/ai-clip-const';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const scene = ref(0);
  const scenes = Object.values(AI_CLIP_TEMPLATE_SCENE_ENUM);
  const list = ref([]);
  const loading = ref(false);

  async function load() {
    loading.value = true;
    try {
      const res = await aiClipApi.queryTemplate({ scene: scene.value || undefined });
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
  .tpl {
    cursor: pointer;
  }
  .tpl img {
    width: 100%;
    height: 150px;
    object-fit: cover;
    border-radius: 8px;
  }
</style>
