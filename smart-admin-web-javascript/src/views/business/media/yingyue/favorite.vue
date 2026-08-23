<template>
  <div>
    <h3>我的片单</h3>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="片单为空，在详情页加入后会同步到云端" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="4" v-for="item in list" :key="item.videoId">
          <MediaCard :cover="item.coverUrl" :title="item.title" :sub="item.updateInfo" :score="item.score" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)" />
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>
<script setup>
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { useMediaList } from '/@/views/business/media/use-media-online';
  import MediaCard from './components/media-card.vue';
  const { list, loading } = useMediaList(() => yingyueApi.favorite());
</script>
