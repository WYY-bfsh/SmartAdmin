<template>
  <div class="lyric">
    <p v-if="!lines.length">暂无歌词，请保持联网后重新打开本页</p>
    <p v-for="(line, i) in lines" :key="i" :class="{ on: i === active }">{{ line.text }}</p>
  </div>
</template>
<script setup>
  import { computed } from 'vue';
  import { useMusicPlayerStore } from '/@/store/modules/business/music-player';

  const store = useMusicPlayerStore();
  const lines = computed(() => store.currentSong?.lyrics || []);
  const active = computed(() => {
    const t = store.currentTime;
    let idx = 0;
    lines.value.forEach((line, i) => {
      if (t >= line.time) idx = i;
    });
    return idx;
  });
</script>
<style scoped>
  .lyric { text-align: center; padding: 24px; }
  .lyric p { color: #888; margin: 8px 0; }
  .lyric p.on { color: #ec4141; font-size: 18px; font-weight: 600; }
</style>
