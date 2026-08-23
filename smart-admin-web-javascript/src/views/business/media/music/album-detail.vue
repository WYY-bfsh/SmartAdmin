<template>
  <div class="music-page">
    <a-spin :spinning="loading">
      <a-empty v-if="!album.albumId" description="专辑不存在或网络异常" />
      <template v-else>
        <div class="head">
          <img :src="album.coverUrl" alt="" />
          <div>
            <h2>{{ album.name }}</h2>
            <p>{{ album.artist }} · {{ album.year }}</p>
            <a-button type="primary" :disabled="!songs.length" @click="store.playList(songs, 0)">播放全部</a-button>
          </div>
        </div>
        <SongTable :songs="songs" />
      </template>
    </a-spin>
    <PlayerBar />
  </div>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { musicApi } from '/@/api/business/media/music-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { useMusicPlayerStore } from '/@/store/modules/business/music-player';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SongTable from './components/song-table.vue';
  import PlayerBar from './components/player-bar.vue';

  const route = useRoute();
  const store = useMusicPlayerStore();
  const loading = ref(false);
  const album = ref({});
  const songs = ref([]);

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await musicApi.albumDetail(route.params.id);
      album.value = res.data || {};
      songs.value = unwrapList(res.data?.songs);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
  .head { display: flex; gap: 20px; margin-bottom: 20px; }
  .head img { width: 180px; height: 180px; border-radius: 8px; object-fit: cover; }
</style>
