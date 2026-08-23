<template>
  <div>
    <a-radio-group v-model:value="category" button-style="solid" style="margin-bottom: 16px" @change="load">
      <a-radio-button v-for="item in cats" :key="item.value" :value="item.value">{{ item.desc }}</a-radio-button>
    </a-radio-group>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="频道内容需联网加载" />
      <a-row :gutter="[16, 16]" v-else>
        <a-col :span="4" v-for="item in list" :key="item.videoId">
          <MediaCard :cover="item.coverUrl" :title="item.title" :sub="item.updateInfo" :score="item.score" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)" />
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import { YINGYUE_CATEGORY_ENUM } from '/@/constants/business/media/yingyue-const';
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import MediaCard from './components/media-card.vue';

  const cats = Object.values(YINGYUE_CATEGORY_ENUM);
  const category = ref(1);
  const list = ref([]);
  const loading = ref(false);

  async function load() {
    loading.value = true;
    try {
      const res = await yingyueApi.channel(category.value);
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
