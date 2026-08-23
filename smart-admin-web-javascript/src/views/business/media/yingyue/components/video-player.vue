<template>
  <video
    ref="videoRef"
    class="player"
    :src="currentSrc"
    controls
    autoplay
    @timeupdate="onTime"
    @error="onError"
  />
  <div class="bar">
    <a-space>
      <a-select v-model:value="quality" style="width: 120px" :options="qualities" />
      <a-select v-model:value="rate" style="width: 100px" :options="rates" @change="onRate" />
      <span>进度 {{ progress }}%</span>
    </a-space>
  </div>
</template>

<script setup>
  import { nextTick, ref, watch } from 'vue';
  import { message } from 'ant-design-vue';

  const props = defineProps({
    src: String,
  });
  const emit = defineEmits(['progress']);
  const FALLBACKS = [
    'https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4',
    'https://www.runoob.com/try/demo_source/movie.mp4',
    'https://media.w3.org/2010/05/sintel/trailer.mp4',
  ];
  const videoRef = ref();
  const currentSrc = ref(props.src);
  const failIndex = ref(-1);
  const quality = ref('1080p');
  const rate = ref(1);
  const progress = ref(0);
  const qualities = [
    { label: '标清 480P', value: '480p' },
    { label: '高清 720P', value: '720p' },
    { label: '超清 1080P', value: '1080p' },
    { label: '蓝光 4K', value: '4k' },
  ];
  const rates = [0.75, 1, 1.25, 1.5, 2].map((v) => ({ label: `${v}x`, value: v }));

  watch(
    () => props.src,
    (val) => {
      failIndex.value = -1;
      currentSrc.value = val;
    }
  );

  watch(currentSrc, async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    const el = videoRef.value;
    if (!el) {
      return;
    }
    el.load();
    el.play?.().catch(() => {});
  });

  function onRate(val) {
    if (videoRef.value) {
      videoRef.value.playbackRate = val;
    }
  }

  function onError() {
    const next = failIndex.value + 1;
    if (next < FALLBACKS.length) {
      failIndex.value = next;
      currentSrc.value = FALLBACKS[next];
      return;
    }
    message.error('当前影片无法播放，请换一部或检查网络');
  }

  function onTime(e) {
    const el = e.target;
    if (!el.duration) return;
    const p = Math.round((el.currentTime / el.duration) * 100);
    progress.value = p;
    emit('progress', p);
  }
</script>

<style scoped>
  .player {
    width: 100%;
    background: #000;
    border-radius: 8px;
    max-height: 62vh;
  }
  .bar {
    margin-top: 10px;
    color: #ccc;
  }
</style>
