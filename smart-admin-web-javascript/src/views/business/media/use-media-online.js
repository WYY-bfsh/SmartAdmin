import { onMounted, ref } from 'vue';
import { smartSentry } from '/@/lib/smart-sentry';

export function unwrapList(data) {
  if (Array.isArray(data)) {
    return data;
  }
  if (data && Array.isArray(data.list)) {
    return data.list;
  }
  return [];
}

export function useMediaList(loader) {
  const list = ref([]);
  const loading = ref(false);

  async function load() {
    loading.value = true;
    try {
      const res = await loader();
      list.value = unwrapList(res?.data);
    } catch (e) {
      smartSentry.captureError(e);
      list.value = [];
    } finally {
      loading.value = false;
    }
  }

  onMounted(load);
  return { list, loading, load };
}

export async function loadOnline(request) {
  const res = await request();
  return res?.data;
}
