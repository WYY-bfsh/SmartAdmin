<template>
  <div class="music-page">
    <h3>排行榜</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="排行榜需联网加载" />
      <SongTable v-else :songs="list" />
    </a-spin>
    <PlayerBar />
  </div>
</template>
<script setup>
  import { musicApi } from '/@/api/business/media/music-api';
  import { useMediaList } from '/@/views/business/media/use-media-online';
  import SongTable from './components/song-table.vue';
  import PlayerBar from './components/player-bar.vue';
  const { list, loading } = useMediaList(() => musicApi.rank(1));
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
</style>
