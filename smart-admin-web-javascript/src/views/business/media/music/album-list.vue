<template>
  <div class="music-page">
    <h3>专辑</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="专辑需联网加载" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="6" v-for="item in list" :key="item.albumId">
          <CoverPlay :cover="item.coverUrl" :title="item.name" :sub="`${item.artist} · ${item.year}`" @click="$router.push(`/media/music/album/${item.albumId}`)" />
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
  const { list, loading } = useMediaList(() => musicApi.queryAlbum({}));
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
</style>
