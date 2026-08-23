<template>
  <div>
    <FilterBar :model-value="filter" @update:model-value="(v) => Object.assign(filter, v)" @change="load" />
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="片库需联网加载" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="4" v-for="item in list" :key="item.videoId">
          <MediaCard :cover="item.coverUrl" :title="item.title" :sub="item.updateInfo" :extra="item.year + ''" :score="item.score" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)" />
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import FilterBar from './components/filter-bar.vue';
  import MediaCard from './components/media-card.vue';

  const filter = reactive({ category: undefined, area: undefined });
  const list = ref([]);
  const loading = ref(false);

  async function load() {
    loading.value = true;
    try {
      const res = await yingyueApi.queryLibrary({ category: filter.category, area: filter.area });
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }

  onMounted(load);
</script>
