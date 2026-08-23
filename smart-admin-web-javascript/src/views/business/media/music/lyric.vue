<template>
  <div class="lyric-page">
    <a-empty v-if="!song" description="请先联网播放一首歌" />
    <template v-else>
      <img class="cover" :src="song.coverUrl" alt="" />
      <div>
        <h2>{{ song.name }}</h2>
        <p>{{ song.artist }} · {{ song.album }}</p>
        <LyricPanel />
      </div>
    </template>
    <PlayerBar />
  </div>
</template>
<script setup>
  import { computed } from 'vue';
  import { useMusicPlayerStore } from '/@/store/modules/business/music-player';
  import LyricPanel from './components/lyric-panel.vue';
  import PlayerBar from './components/player-bar.vue';
  const store = useMusicPlayerStore();
  const song = computed(() => store.currentSong);
</script>
<style scoped>
  .lyric-page {
    display: flex;
    gap: 40px;
    padding: 24px 24px 100px;
    min-height: 480px;
    background: linear-gradient(180deg, #1a1a1a, #111);
    color: #fff;
    border-radius: 8px;
  }
  .cover {
    width: 280px;
    height: 280px;
    border-radius: 12px;
    object-fit: cover;
  }
</style>
