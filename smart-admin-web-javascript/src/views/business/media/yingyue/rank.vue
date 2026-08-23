<template>
  <div>
    <h3>热播榜</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="排行榜需联网加载" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="4" v-for="(item, index) in list" :key="item.videoId">
          <MediaCard :cover="item.coverUrl" :title="`${index + 1}. ${item.title}`" :sub="'评分 ' + item.score" :score="item.score" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)" />
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>
<script setup>
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { useMediaList } from '/@/views/business/media/use-media-online';
  import MediaCard from './components/media-card.vue';
  const { list, loading } = useMediaList(() => yingyueApi.rank());
</script>
