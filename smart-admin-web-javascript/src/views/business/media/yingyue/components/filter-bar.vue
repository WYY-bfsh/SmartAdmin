<template>
  <div class="filter-bar">
    <div class="row">
      <span>类型</span>
      <a-radio-group :value="modelValue.category" button-style="solid" @change="(e) => emitChange('category', e.target.value)">
        <a-radio-button :value="undefined">全部</a-radio-button>
        <a-radio-button v-for="item in categories" :key="item.value" :value="item.value">{{ item.desc }}</a-radio-button>
      </a-radio-group>
    </div>
    <div class="row">
      <span>地区</span>
      <a-radio-group :value="modelValue.area" button-style="solid" @change="(e) => emitChange('area', e.target.value)">
        <a-radio-button :value="undefined">全部</a-radio-button>
        <a-radio-button v-for="item in areas" :key="item.value" :value="item.value">{{ item.desc }}</a-radio-button>
      </a-radio-group>
    </div>
  </div>
</template>

<script setup>
  import { YINGYUE_AREA_ENUM, YINGYUE_CATEGORY_ENUM } from '/@/constants/business/media/yingyue-const';

  const props = defineProps({
    modelValue: { type: Object, default: () => ({}) },
  });
  const emit = defineEmits(['update:modelValue', 'change']);
  const categories = Object.values(YINGYUE_CATEGORY_ENUM);
  const areas = Object.values(YINGYUE_AREA_ENUM);

  function emitChange(key, value) {
    const next = { ...props.modelValue, [key]: value };
    emit('update:modelValue', next);
    emit('change', next);
  }
</script>

<style scoped>
  .filter-bar {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 16px;
  }
  .row {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .row > span {
    width: 40px;
    color: #888;
  }
</style>
