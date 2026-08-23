<template>
  <a-card size="small" :bordered="false" title="脚本工坊">
    <a-button type="primary" class="mb" @click="open()">新建口播脚本</a-button>
    <a-spin :spinning="loading">
      <a-empty v-if="!list.length" description="没有脚本，请联网后新建" />
      <a-list v-else :dataSource="list" item-layout="vertical">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta :title="item.title" :description="`${item.wordCount} 字 · ${item.updateTime}`" />
            <div>{{ item.content }}</div>
            <template #actions>
              <a @click="open(item)">编辑</a>
              <a @click="$router.push('/media/ai-clip/task')">生成成片</a>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-spin>
    <a-modal v-model:open="visible" title="口播脚本" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="标题"><a-input v-model:value="form.title" /></a-form-item>
        <a-form-item label="文案"><a-textarea v-model:value="form.content" :rows="6" /></a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { unwrapList } from '/@/views/business/media/use-media-online';
  import { smartSentry } from '/@/lib/smart-sentry';

  const list = ref([]);
  const loading = ref(false);
  const visible = ref(false);
  const form = reactive({ scriptId: null, title: '', content: '' });

  async function load() {
    loading.value = true;
    try {
      const res = await aiClipApi.queryScript({});
      list.value = unwrapList(res.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }

  function open(item) {
    form.scriptId = item?.scriptId || null;
    form.title = item?.title || '';
    form.content = item?.content || '';
    visible.value = true;
  }

  async function save() {
    try {
      await aiClipApi.saveScript({ scriptId: form.scriptId, title: form.title, content: form.content });
      visible.value = false;
      message.success('脚本已保存到云端');
      await load();
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  onMounted(load);
</script>

<style scoped>
  .mb {
    margin-bottom: 12px;
  }
</style>
