<template>
  <div class="music-page">
    <a-input-search v-model:value="keyword" placeholder="搜索全网歌曲 / 歌手 / 专辑，例如：虹之间" enter-button @search="onSearch" style="max-width: 520px" />
    <h3>搜索结果</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" :description="emptyText" />
      <SongTable v-else :songs="list" />
    </a-spin>
    <PlayerBar />
  </div>
</template>
<script setup>
  import { computed, ref } from 'vue';
  import { musicApi } from '/@/api/business/media/music-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SongTable from './components/song-table.vue';
  import PlayerBar from './components/player-bar.vue';

  const keyword = ref('');
  const list = ref([]);
  const loading = ref(false);
  const searched = ref(false);
  const emptyText = computed(() => {
    if (!searched.value) {
      return '输入歌名后回车，将联网搜索全网曲库';
    }
    return `没有找到「${keyword.value}」，请换个关键词或检查后端能否访问外网`;
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
      const res = await musicApi.search(keyword.value.trim());
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
  h3 { margin-top: 16px; }
</style>
