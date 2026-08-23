<template>
  <div>
    <a-input-search v-model:value="keyword" placeholder="联网搜索影片 / 剧集，例如：流浪地球" enter-button @search="onSearch" style="max-width: 520px" />
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" :description="emptyText" style="margin-top: 16px" />
      <a-row :gutter="[16, 16]" style="margin-top: 16px" v-else>
        <a-col :span="4" v-for="item in list" :key="item.videoId">
          <MediaCard :cover="item.coverUrl" :title="item.title" :sub="item.updateInfo" :score="item.score" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)" />
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>
<script setup>
  import { computed, ref } from 'vue';
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import MediaCard from './components/media-card.vue';

  const keyword = ref('');
  const list = ref([]);
  const loading = ref(false);
  const searched = ref(false);
  const emptyText = computed(() => {
    if (!searched.value) {
      return '输入片名后回车，将联网搜索全网片库';
    }
    return `没有找到「${keyword.value}」，请换个关键词`;
  });

  async function onSearch() {
    if (!keyword.value.trim()) {
      searched.value = false;
      list.value = [];
      return;
    }
    searched.value = true;
    loading.value = true;
    try {
      const res = await yingyueApi.search(keyword.value.trim());
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }
</script>
