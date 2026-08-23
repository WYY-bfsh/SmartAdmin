<template>
  <div class="player-bar" v-if="song">
    <audio
      ref="audioRef"
      :src="playSrc"
      preload="auto"
      :loop="mode === MUSIC_PLAY_MODE_ENUM.LOOP.value"
      @timeupdate="onTime"
      @ended="onEnded"
      @loadedmetadata="onMeta"
      @canplay="onCanPlay"
      @error="onError"
    />
    <img class="cover" :src="song.coverUrl" alt="" @click="goLyric" />
    <div class="meta">
      <div class="name">{{ song.name }}</div>
      <div class="artist">{{ song.artist }}</div>
    </div>
    <HeartOutlined class="like" :class="{ on: store.isLiked }" @click="store.toggleLike()" />
    <div class="controls">
      <StepBackwardOutlined @click="store.prev()" />
      <span class="play" @click="toggle">
        <PauseCircleFilled v-if="store.playing" />
        <PlayCircleFilled v-else />
      </span>
      <StepForwardOutlined @click="store.next()" />
    </div>
    <div class="progress">
      <span>{{ fmt(store.currentTime) }}</span>
      <a-slider :value="store.currentTime" :max="store.duration || 1" :tooltip-open="false" @change="seek" />
      <span>{{ fmt(store.duration) }}</span>
    </div>
    <div class="mode" @click="store.cycleMode()">{{ modeLabel }}</div>
    <SoundOutlined />
    <a-slider class="vol" :value="store.volume * 100" :tooltip-open="false" @change="onVol" />
  </div>
</template>

<script setup>
  import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { HeartOutlined, PauseCircleFilled, PlayCircleFilled, SoundOutlined, StepBackwardOutlined, StepForwardOutlined } from '@ant-design/icons-vue';
  import { MUSIC_PLAY_MODE_ENUM } from '/@/constants/business/media/music-const';
  import { useMusicPlayerStore } from '/@/store/modules/business/music-player';
  import { musicApi } from '/@/api/business/media/music-api';

  const store = useMusicPlayerStore();
  const router = useRouter();
  const audioRef = ref();
  const song = computed(() => store.currentSong);
  const playSrc = computed(() => (song.value?.songId ? musicApi.streamUrl(song.value.songId) : ''));
  const mode = computed(() => store.mode);
  const modeLabel = computed(() => {
    if (mode.value === MUSIC_PLAY_MODE_ENUM.LOOP.value) return '单曲';
    if (mode.value === MUSIC_PLAY_MODE_ENUM.RANDOM.value) return '随机';
    return '顺序';
  });

  function fmt(sec) {
    const s = Math.max(0, Math.floor(sec || 0));
    return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
  }

  function goLyric() {
    router.push('/media/music/lyric');
  }

  function toggle() {
    store.togglePlay();
  }

  function seek(val) {
    if (audioRef.value) {
      audioRef.value.currentTime = val;
      store.currentTime = val;
    }
  }

  function onTime(e) {
    store.currentTime = e.target.currentTime;
  }

  function onMeta(e) {
    const d = e.target.duration;
    store.duration = Number.isFinite(d) ? d : 0;
    if (audioRef.value) {
      audioRef.value.volume = store.volume;
    }
  }

  function onCanPlay() {
    if (store.playing && audioRef.value?.paused) {
      audioRef.value.play().catch((e) => {
        store.playing = false;
        message.error(e?.message || '浏览器拦截了自动播放，请再点一次播放');
      });
    }
  }

  function onError() {
    store.playing = false;
    message.error('当前曲目无法播放（无版权或音源被拦截），请换一首');
  }

  function onEnded() {
    if (mode.value !== MUSIC_PLAY_MODE_ENUM.LOOP.value) {
      store.next();
    }
  }

  function onVol(val) {
    store.volume = val / 100;
    if (audioRef.value) {
      audioRef.value.volume = store.volume;
    }
  }

  watch(
    () => store.playing,
    (playing) => {
      const el = audioRef.value;
      if (!el) return;
      el.volume = store.volume;
      if (playing) {
        el.play().catch((e) => {
          store.playing = false;
          message.error(e?.message || '播放失败，请再点一次');
        });
      } else {
        el.pause();
      }
    }
  );

  watch(
    () => song.value?.songId,
    async () => {
      await nextTick();
      const el = audioRef.value;
      if (!el) return;
      store.currentTime = 0;
      store.duration = 0;
      el.volume = store.volume;
      el.load();
    }
  );

  onMounted(() => {
    store.bootstrap();
    if (audioRef.value) {
      audioRef.value.volume = store.volume;
    }
  });

  onBeforeUnmount(() => {
    audioRef.value?.pause();
  });
</script>

<style scoped>
  .player-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 40;
    height: 72px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0 20px;
    background: #1a1a1a;
    color: #fff;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.25);
  }
  .cover {
    width: 52px;
    height: 52px;
    border-radius: 6px;
    object-fit: cover;
    cursor: pointer;
  }
  .meta {
    width: 160px;
  }
  .name {
    font-weight: 600;
  }
  .artist {
    font-size: 12px;
    color: #bbb;
  }
  .like.on {
    color: #e74c3c;
  }
  .controls {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 20px;
    cursor: pointer;
  }
  .play {
    font-size: 36px;
    color: #ec4141;
  }
  .progress {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .progress :deep(.ant-slider) {
    flex: 1;
    margin: 0;
  }
  .mode {
    cursor: pointer;
    font-size: 12px;
    color: #ccc;
    width: 36px;
  }
  .vol {
    width: 90px;
    margin: 0;
  }
</style>
