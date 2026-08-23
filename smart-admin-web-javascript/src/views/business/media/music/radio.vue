<template>
  <div class="music-page">
    <h3>播客电台</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="电台需联网加载" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="8" v-for="item in list" :key="item.radioId">
          <a-card>
            <div class="radio">
              <img :src="item.coverUrl" alt="" />
              <div>
                <b>{{ item.name }}</b>
                <div>{{ item.host }} · {{ item.playing }}</div>
                <a-button type="link" @click="playRadio">收听</a-button>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
    <PlayerBar />
  </div>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import { musicApi } from '/@/api/business/media/music-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { useMusicPlayerStore } from '/@/store/modules/business/music-player';
  import { smartSentry } from '/@/lib/smart-sentry';
  import PlayerBar from './components/player-bar.vue';

  const store = useMusicPlayerStore();
  const list = ref([]);
  const loading = ref(false);
  const songs = ref([]);

  async function playRadio() {
    if (!songs.value.length) {
      const res = await musicApi.querySongs();
      songs.value = unwrapList(res.data);
    }
    store.playList(songs.value, 0);
  }

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await musicApi.radio();
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>
<style scoped>
  .music-page { padding-bottom: 90px; }
  .radio { display: flex; gap: 12px; }
  .radio img { width: 88px; height: 88px; border-radius: 8px; object-fit: cover; }
</style>
