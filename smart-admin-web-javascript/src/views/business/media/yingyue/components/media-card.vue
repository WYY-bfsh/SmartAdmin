<template>
  <div class="media-card" @click="$emit('click')">
    <div class="poster">
      <img :src="cover" alt="" />
      <div class="score" v-if="score">{{ score }}</div>
      <div class="info">{{ extra }}</div>
    </div>
    <div class="title">{{ title }}</div>
    <div class="sub" v-if="sub">{{ sub }}</div>
  </div>
</template>

<script setup>
  defineProps({
    cover: String,
    title: String,
    sub: String,
    extra: String,
    score: [String, Number],
  });
  defineEmits(['click']);
</script>

<style scoped>
  .media-card {
    cursor: pointer;
  }
  .poster {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
  }
  .poster img {
    width: 100%;
    aspect-ratio: 3 / 4;
    object-fit: cover;
    display: block;
  }
  .score {
    position: absolute;
    right: 8px;
    top: 8px;
    background: rgba(0, 0, 0, 0.65);
    color: #ffb400;
    padding: 0 6px;
    border-radius: 4px;
    font-weight: 600;
  }
  .info {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 6px 8px;
    color: #fff;
    font-size: 12px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  }
  .title {
    margin-top: 8px;
    font-weight: 600;
  }
  .sub {
    color: #888;
    font-size: 12px;
  }
</style>
