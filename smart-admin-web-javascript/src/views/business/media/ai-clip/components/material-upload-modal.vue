<template>
  <a-modal :open="visible" title="上传素材到云端" :confirm-loading="uploading" @ok="ok" @cancel="visible = false">
    <p class="tip">文件将上传到媒体中心公网目录，上传后即可在时间线预览播放。</p>
    <a-upload-dragger
      :multiple="true"
      :max-count="8"
      :before-upload="beforeUpload"
      :custom-request="customRequest"
      accept=".mp4,.mov,.webm,.jpg,.jpeg,.png,.gif,.webp,.mp3,.wav,.aac,.m4a"
    >
      <p>把视频 / 图片 / 音频拖到这里，单文件不超过 200MB</p>
    </a-upload-dragger>
  </a-modal>
</template>

<script setup>
  import { ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { aiClipApi } from '/@/api/business/media/ai-clip-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const emit = defineEmits(['ok']);
  const visible = ref(false);
  const uploading = ref(false);
  function showModal() {
    visible.value = true;
  }

  function beforeUpload(file) {
    const isLimit = file.size / 1024 / 1024 < 200;
    if (!isLimit) {
      message.error('单个文件不能超过 200MB');
    }
    return isLimit;
  }

  async function customRequest(options) {
    const formData = new FormData();
    formData.append('file', options.file);
    try {
      await aiClipApi.uploadMaterial(formData);
      options.onSuccess?.();
    } catch (e) {
      smartSentry.captureError(e);
      options.onError?.(e);
    }
  }

  async function ok() {
    uploading.value = true;
    try {
      visible.value = false;
      message.success('素材已上传到云端');
      emit('ok');
    } finally {
      uploading.value = false;
    }
  }

  defineExpose({ showModal });
</script>

<style scoped>
  .tip {
    color: #666;
    margin-bottom: 12px;
  }
</style>
