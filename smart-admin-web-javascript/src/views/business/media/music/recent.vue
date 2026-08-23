<template>
  <div class="music-page">
    <h3>最近播放</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="还没有播放记录，联网播放后会记在云端" />
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
  const { list, loading } = useMediaList(() => musicApi.recent());
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
</style>
