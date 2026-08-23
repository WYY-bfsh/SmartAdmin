<template>
  <a-card size="small" title="观看历史">
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="还没有观看记录，播放后会同步到云端" />
      <div class="row" v-for="item in list" :key="item.videoId" @click="$router.push(`/media/yingyue/play/${item.videoId}`)">
        <img :src="item.coverUrl" alt="" />
        <div>
          <b>{{ item.title }}</b>
          <div>第 {{ item.episodeNo }} 集</div>
          <a-progress :percent="item.progress" size="small" />
          <div class="time">{{ item.updateTime }}</div>
        </div>
      </div>
    </a-spin>
  </a-card>
</template>
<script setup>
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { useMediaList } from '/@/views/business/media/use-media-online';
  const { list, loading } = useMediaList(() => yingyueApi.history());
</script>
<style scoped>
  .row { display: flex; gap: 16px; margin-bottom: 16px; cursor: pointer; }
  .row img { width: 200px; height: 112px; object-fit: cover; border-radius: 6px; }
  .time { color: #888; font-size: 12px; }
</style>
