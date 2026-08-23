<template>
  <a-modal :open="visible" title="编辑脚本" @ok="ok" @cancel="visible = false">
    <a-textarea v-model:value="content" :rows="8" />
  </a-modal>
</template>
<script setup>
  import { ref } from 'vue';
  const visible = ref(false);
  const content = ref('');
  function showModal(text) {
    content.value = text || '';
    visible.value = true;
  }
  function ok() {
    visible.value = false;
  }
  defineExpose({ showModal });
</script>
