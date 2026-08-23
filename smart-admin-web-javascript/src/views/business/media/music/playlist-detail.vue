<template>
  <div class="music-page">
    <a-spin :spinning="loading">
      <a-empty v-if="!playlist.playlistId" description="歌单不存在或网络异常" />
      <template v-else>
        <div class="head">
          <img :src="playlist.coverUrl" alt="" />
          <div>
            <h2>{{ playlist.name }}</h2>
            <p>{{ playlist.description }}</p>
            <a-button type="primary" :disabled="!songs.length" @click="playAll">播放全部</a-button>
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
  const playlist = ref({});
  const songs = ref([]);

  function playAll() {
    store.playList(songs.value, 0);
  }

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await musicApi.playlistDetail(route.params.id);
      const data = res.data || {};
      playlist.value = data.playlistId ? data : data.playlist || data;
      songs.value = unwrapList(data.songs);
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
