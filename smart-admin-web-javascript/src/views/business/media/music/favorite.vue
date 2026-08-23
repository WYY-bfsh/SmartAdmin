<template>
  <div class="music-page">
    <h3>我喜欢的音乐</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="还没有喜欢的歌曲，播放后点红心会同步到云端" />
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
  const { list, loading } = useMediaList(() => musicApi.favorite());
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
</style>
