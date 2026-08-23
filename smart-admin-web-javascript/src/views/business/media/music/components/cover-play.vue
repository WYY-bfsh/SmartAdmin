<template>
  <div class="cover-card" @click="$emit('click')">
    <div class="cover-wrap">
      <img :src="cover" alt="" />
      <div class="mask"><PlayCircleFilled /></div>
    </div>
    <div class="title">{{ title }}</div>
    <div class="sub" v-if="sub">{{ sub }}</div>
  </div>
</template>

<script setup>
  import { PlayCircleFilled } from '@ant-design/icons-vue';

  defineProps({
    cover: String,
    title: String,
    sub: String,
  });
  defineEmits(['click']);
</script>

<style scoped>
  .cover-card {
    cursor: pointer;
  }
  .cover-wrap {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
  }
  .cover-wrap img {
    width: 100%;
    aspect-ratio: 1;
    object-fit: cover;
    display: block;
  }
  .mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 36px;
    background: rgba(0, 0, 0, 0.25);
    opacity: 0;
    transition: 0.2s;
  }
  .cover-card:hover .mask {
    opacity: 1;
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
