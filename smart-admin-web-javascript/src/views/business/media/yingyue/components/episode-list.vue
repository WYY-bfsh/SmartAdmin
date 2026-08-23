<template>
  <div class="eps">
    <a-button
      v-for="n in count"
      :key="n"
      :type="n === current ? 'primary' : 'default'"
      @click="$emit('change', n)"
    >
      {{ n }}
    </a-button>
  </div>
</template>

<script setup>
  defineProps({
    count: { type: Number, default: 1 },
    current: { type: Number, default: 1 },
  });
  defineEmits(['change']);
</script>

<style scoped>
  .eps {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
</style>
