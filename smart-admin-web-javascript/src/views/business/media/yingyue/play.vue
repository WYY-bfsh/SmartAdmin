<template>
  <div class="play-page">
    <a-spin :spinning="loading">
      <a-empty v-if="!video.videoId" description="播放地址需从云端获取，请检查网络" />
      <template v-else>
        <VideoPlayer :src="playerSrc" @progress="onProgress" />
        <div class="meta">
          <h2>{{ video.title }}</h2>
          <p v-if="usingDemoClip" class="hint">正片受版权限制，当前播放公开演示片源，不是原片。</p>
          <p>{{ video.intro }}</p>
          <h4>选集</h4>
          <EpisodeList :count="video.episodeCount" :current="ep" @change="onEp" />
        </div>
      </template>
    </a-spin>
  </div>
</template>
<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import VideoPlayer from './components/video-player.vue';
  import EpisodeList from './components/episode-list.vue';

  const route = useRoute();
  const router = useRouter();
  const DEMO_CLIP = 'https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4';
  const loading = ref(false);
  const video = ref({});
  const ep = computed(() => Number(route.query.ep || 1));
  const usingDemoClip = computed(() => {
    const playUrl = String(video.value.playUrl || '');
    const info = String(video.value.updateInfo || '');
    return !playUrl || /googleapis|gtv-videos-bucket|huoshanstatic|runoob|w3\.org/.test(playUrl) || /版权/.test(info);
  });
  const playerSrc = computed(() => {
    if (!video.value.videoId) {
      return '';
    }
    const playUrl = String(video.value.playUrl || '');
    if (/huoshanstatic|runoob\.com|w3\.org/.test(playUrl)) {
      return playUrl;
    }
    if (!playUrl || /googleapis|gtv-videos-bucket/.test(playUrl)) {
      return DEMO_CLIP;
    }
    return yingyueApi.streamUrl(video.value.videoId);
  });

  function report(progress) {
    yingyueApi.reportProgress({
      videoId: video.value.videoId,
      episodeNo: ep.value,
      progress,
    }).catch((e) => smartSentry.captureError(e));
  }

  function onProgress(p) {
    if (p % 10 === 0) {
      report(p);
    }
  }

  function onEp(n) {
    router.replace({ query: { ep: n } });
    report(0);
  }

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await yingyueApi.play(route.params.id, ep.value);
      video.value = res.data || {};
      report(1);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>
<style scoped>
  .play-page { background: #111; color: #eee; padding: 12px; border-radius: 8px; }
  .meta { margin-top: 16px; }
  .hint { color: #faad14; }
</style>
