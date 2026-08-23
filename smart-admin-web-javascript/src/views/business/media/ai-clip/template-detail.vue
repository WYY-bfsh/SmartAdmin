<template>
  <a-card size="small" :bordered="false">
    <a-spin :spinning="loading">
      <a-empty v-if="!tpl.templateId" description="模板不存在或网络异常" />
      <template v-else>
        <img :src="tpl.coverUrl" class="banner" alt="" />
        <h2>{{ tpl.name }}</h2>
        <p>时长 {{ tpl.duration }} · 使用 {{ tpl.useCount }} 次</p>
        <a-space>
          <a-button type="primary" @click="createFromTemplate">用此模板创作</a-button>
          <a-button @click="$router.back()">返回</a-button>
        </a-space>
      </template>
    </a-spin>
  </a-card>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const route = useRoute();
  const router = useRouter();
  const loading = ref(false);
  const tpl = ref({});

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await aiClipApi.templateDetail(route.params.id);
      tpl.value = res.data || {};
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });

  async function createFromTemplate() {
    try {
      const res = await aiClipApi.saveProject({
        title: `${tpl.value.name} · 新作品`,
        scene: tpl.value.scene,
        status: 10,
        ratio: '9:16',
      });
      message.success('已按模板创建云端草稿');
      router.push(`/media/ai-clip/project/editor/${res.data}`);
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
</script>

<style scoped>
  .banner {
    width: 100%;
    max-height: 280px;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 12px;
  }
</style>
