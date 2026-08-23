<template>
  <div class="track-row">
    <span>{{ name }}</span>
    <div class="bar" />
  </div>
</template>
<script setup>
  defineProps({ name: String });
</script>
<style scoped>
  .track-row {
    display: flex;
    gap: 8px;
    align-items: center;
    margin-bottom: 6px;
  }
  .bar {
    flex: 1;
    height: 18px;
    background: #333;
    border-radius: 4px;
  }
</style>
