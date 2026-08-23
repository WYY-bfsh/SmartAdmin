<template>
  <div class="yy">
    <a-spin :spinning="loading">
      <a-empty v-if="!videos.length" description="影月片库需联网加载" />
      <template v-else>
        <a-carousel autoplay>
          <div v-for="item in banners" :key="item.videoId" class="banner" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)">
            <img :src="item.coverUrl" alt="" />
            <div class="cap">
              <h2>{{ item.title }}</h2>
              <p>{{ item.subTitle }}</p>
            </div>
          </div>
        </a-carousel>
        <h3>继续观看</h3>
        <a-empty v-if="!history.length" description="还没有观看记录" />
        <a-row :gutter="16" v-else>
          <a-col :span="8" v-for="item in history" :key="item.videoId">
            <div class="hist" @click="$router.push(`/media/yingyue/play/${item.videoId}`)">
              <img :src="item.coverUrl" alt="" />
              <div>
                <b>{{ item.title }}</b>
                <div>第 {{ item.episodeNo }} 集 · 看到 {{ item.progress }}%</div>
              </div>
            </div>
          </a-col>
        </a-row>
        <h3>热播推荐</h3>
        <a-row :gutter="[16, 16]">
          <a-col :span="4" v-for="item in videos" :key="item.videoId">
            <MediaCard :cover="item.coverUrl" :title="item.title" :sub="item.updateInfo" :extra="item.year + ''" :score="item.score" @click="$router.push(`/media/yingyue/detail/${item.videoId}`)" />
          </a-col>
        </a-row>
      </template>
    </a-spin>
  </div>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import { yingyueApi } from '/@/api/business/media/yingyue-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';
  import MediaCard from './components/media-card.vue';

  const loading = ref(false);
  const banners = ref([]);
  const history = ref([]);
  const videos = ref([]);

  onMounted(async () => {
    loading.value = true;
    try {
      const res = await yingyueApi.home();
      banners.value = unwrapList(res.data?.banners);
      history.value = unwrapList(res.data?.history);
      videos.value = unwrapList(res.data?.videos);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  });
</script>
<style scoped>
  .banner { position: relative; cursor: pointer; }
  .banner img { width: 100%; height: 280px; object-fit: cover; border-radius: 8px; }
  .cap { position: absolute; left: 24px; bottom: 20px; color: #fff; }
  h3 { margin: 20px 0 12px; }
  .hist { display: flex; gap: 10px; cursor: pointer; }
  .hist img { width: 140px; height: 80px; object-fit: cover; border-radius: 6px; }
</style>
