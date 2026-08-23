<template>
  <div class="music-page">
    <a-spin :spinning="loading">
      <a-empty v-if="!banners.length && !songs.length" description="发现页需联网拉取曲库" />
      <template v-else>
        <a-carousel autoplay>
          <div v-for="item in banners" :key="item.id">
            <img class="banner" :src="item.coverUrl" :alt="item.title" />
          </div>
        </a-carousel>
        <h3>推荐歌单</h3>
        <a-row :gutter="[16, 16]">
          <a-col :span="4" v-for="item in playlists" :key="item.playlistId">
            <CoverPlay :cover="item.coverUrl" :title="item.name" :sub="item.playCount + ' 播放'" @click="$router.push(`/media/music/playlist/${item.playlistId}`)" />
          </a-col>
        </a-row>
        <h3>新歌速递</h3>
        <SongTable :songs="songs" />
      </template>
    </a-spin>
    <PlayerBar />
  </div>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { musicApi } from '/@/api/business/media/music-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import CoverPlay from './components/cover-play.vue';
  import SongTable from './components/song-table.vue';
  import PlayerBar from './components/player-bar.vue';

  const loading = ref(false);
  const banners = ref([]);
  const playlists = ref([]);
  const songs = ref([]);

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await musicApi.discover();
      banners.value = unwrapList(res.data?.banners);
      playlists.value = unwrapList(res.data?.playlists);
      songs.value = unwrapList(res.data?.songs);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>

<style scoped>
  .music-page {
    padding-bottom: 90px;
  }
  .banner {
    width: 100%;
    height: 220px;
    object-fit: cover;
    border-radius: 8px;
  }
  h3 {
    margin: 20px 0 12px;
  }
</style>
