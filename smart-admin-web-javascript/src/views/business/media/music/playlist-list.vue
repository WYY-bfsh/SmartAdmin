<template>
  <div class="music-page">
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="歌单需联网加载" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="4" v-for="item in list" :key="item.playlistId">
          <CoverPlay :cover="item.coverUrl" :title="item.name" :sub="item.playCount + ' 播放'" @click="$router.push(`/media/music/playlist/${item.playlistId}`)" />
        </a-col>
      </a-row>
    </a-spin>
    <PlayerBar />
  </div>
</template>
<script setup>
  import { musicApi } from '/@/api/business/media/music-api';
  import { useMediaList } from '/@/views/business/media/use-media-online';
  import CoverPlay from './components/cover-play.vue';
  import PlayerBar from './components/player-bar.vue';
  const { list, loading } = useMediaList(() => musicApi.queryPlaylist({}));
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
</style>
