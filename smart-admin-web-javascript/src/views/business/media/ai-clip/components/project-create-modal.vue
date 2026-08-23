<template>
  <a-modal :open="visible" title="新建作品" @ok="ok" @cancel="visible = false">
    <a-input v-model:value="title" placeholder="作品名称" />
  </a-modal>
</template>
<script setup>
  import { ref } from 'vue';
  const visible = ref(false);
  const title = ref('');
  const emit = defineEmits(['ok']);
  function showModal() {
    title.value = '';
    visible.value = true;
  }
  function ok() {
    visible.value = false;
    emit('ok', title.value);
  }
  defineExpose({ showModal });
</script>
