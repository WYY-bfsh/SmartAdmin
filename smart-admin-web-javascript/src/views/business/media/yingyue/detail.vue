<template>
  <div class="detail">
    <a-spin :spinning="loading">
      <a-empty v-if="!video.videoId" description="影片不存在或网络异常" />
      <template v-else>
        <img class="bg" :src="video.coverUrl" alt="" />
        <div class="info">
          <img class="poster" :src="video.coverUrl" alt="" />
          <div>
            <h1>{{ video.title }}</h1>
            <p>{{ video.year }} · 评分 {{ video.score }} · {{ video.updateInfo }}</p>
            <p>{{ video.intro }}</p>
            <a-space>
              <a-button type="primary" size="large" @click="$router.push(`/media/yingyue/play/${video.videoId}`)">立即播放</a-button>
              <a-button size="large" @click="toggleFav">{{ fav ? '已加片单' : '加入片单' }}</a-button>
            </a-space>
            <h4 style="margin-top: 24px">选集</h4>
            <EpisodeList :count="video.episodeCount" :current="1" @change="(n) => $router.push(`/media/yingyue/play/${video.videoId}?ep=${n}`)" />
          </div>
        </div>
      </template>
    </a-spin>
  </div>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import EpisodeList from './components/episode-list.vue';

  const route = useRoute();
  const loading = ref(false);
  const fav = ref(false);
  const video = ref({});

  async function toggleFav() {
    try {
      const res = await yingyueApi.toggleFavorite(video.value.videoId);
      fav.value = !!res.data;
      message.success(fav.value ? '已加入云端片单' : '已移出片单');
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await yingyueApi.detail(route.params.id);
      video.value = res.data || {};
      fav.value = !!video.value.favorite;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>
<style scoped>
  .detail { position: relative; min-height: 420px; }
  .bg { position: absolute; inset: 0; width: 100%; height: 280px; object-fit: cover; filter: blur(8px) brightness(0.4); }
  .info { position: relative; display: flex; gap: 24px; padding: 32px; color: #fff; }
  .poster { width: 180px; border-radius: 8px; z-index: 1; }
</style>
