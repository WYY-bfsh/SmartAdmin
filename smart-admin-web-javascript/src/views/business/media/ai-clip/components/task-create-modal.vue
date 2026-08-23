<template>
  <a-modal :open="visible" title="提交成片" @ok="ok" @cancel="visible = false">
    <a-select v-model:value="type" style="width: 100%" :options="opts" />
  </a-modal>
</template>
<script setup>
  import { ref } from 'vue';
  const visible = ref(false);
  const type = ref('一键成片');
  const opts = ['一键成片', '口播成片', '图文成片', '踩点成片'].map((v) => ({ label: v, value: v }));
  function showModal() {
    visible.value = true;
  }
  function ok() {
    visible.value = false;
  }
  defineExpose({ showModal });
</script>
