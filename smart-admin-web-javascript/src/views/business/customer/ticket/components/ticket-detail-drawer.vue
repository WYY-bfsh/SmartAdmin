<!--
  * 工单详情/回复抽屉
-->
<template>
  <a-drawer
    :title="'工单详情'"
    :open="visible"
    :width="720"
    @close="onClose"
    :footerStyle="{ textAlign: 'right' }"
  >
    <template #footer>
      <a-space>
        <a-button @click="onClose">关闭</a-button>
        <a-button
          type="primary"
          @click="showReply"
          v-privilege="'cs:ticket:reply'"
          v-if="detail && detail.status !== TICKET_STATUS_ENUM.CLOSED.value"
        >
          回复
        </a-button>
      </a-space>
    </template>

    <a-spin :spinning="loading">
      <template v-if="detail">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="工单编号">{{ detail.ticketNo }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="statusColor(detail.status)">{{ $smartEnumPlugin.getDescByValue('TICKET_STATUS_ENUM', detail.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="类型">{{ $smartEnumPlugin.getDescByValue('TICKET_TYPE_ENUM', detail.ticketType) }}</a-descriptions-item>
          <a-descriptions-item label="优先级">
            <a-tag :color="priorityColor(detail.priority)">{{ $smartEnumPlugin.getDescByValue('TICKET_PRIORITY_ENUM', detail.priority) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="联系人">{{ detail.contactName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ detail.contactPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="联系邮箱" :span="2">{{ detail.contactEmail || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detail.createTime }}</a-descriptions-item>
          <a-descriptions-item label="处理时间">{{ detail.handleTime || '-' }}</a-descriptions-item>
        </a-descriptions>

        <a-divider>工单内容</a-divider>
        <div class="ticket-content">{{ detail.content }}</div>

        <a-divider>沟通记录（{{ detail.replyCount || 0 }}条）</a-divider>
        <div class="message-list">
          <div
            v-for="msg in detail.messageList"
            :key="msg.messageId"
            :class="['message-item', msg.messageType === TICKET_MESSAGE_TYPE_ENUM.SERVICE.value ? 'message-service' : msg.messageType === TICKET_MESSAGE_TYPE_ENUM.SYSTEM.value ? 'message-system' : 'message-customer']"
          >
            <div class="message-header">
              <span class="message-name">{{ msg.createName || '匿名用户' }}</span>
              <a-tag v-if="msg.messageType === TICKET_MESSAGE_TYPE_ENUM.SERVICE.value" color="blue" size="small">客服</a-tag>
              <a-tag v-else-if="msg.messageType === TICKET_MESSAGE_TYPE_ENUM.SYSTEM.value" size="small">系统</a-tag>
              <span class="message-time">{{ msg.createTime }}</span>
            </div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <a-empty v-if="!detail.messageList || detail.messageList.length === 0" description="暂无沟通记录" />
        </div>
      </template>
    </a-spin>

    <a-modal
      :title="'回复工单'"
      :open="replyVisible"
      :confirmLoading="replyLoading"
      @ok="submitReply"
      @cancel="replyVisible = false"
      width="500px"
    >
      <a-form ref="replyFormRef" :model="replyForm" :rules="replyRules">
        <a-form-item label="回复内容" name="content">
          <a-textarea v-model:value="replyForm.content" placeholder="请输入回复内容" :rows="4" :maxlength="2000" showCount />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-drawer>
</template>

<script setup>
  import { ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { ticketApi } from '/@/api/business/customer/ticket-api';
  import { TICKET_STATUS_ENUM, TICKET_MESSAGE_TYPE_ENUM } from '/@/constants/business/customer/customer-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  const emit = defineEmits(['reloadList']);

  const visible = ref(false);
  const loading = ref(false);
  const detail = ref(null);
  const replyVisible = ref(false);
  const replyLoading = ref(false);
  const replyFormRef = ref();
  const replyForm = ref({ ticketId: null, content: '' });

  const replyRules = {
    content: [{ required: true, message: '请输入回复内容' }],
  };

  function statusColor(status) {
    const map = {
      [TICKET_STATUS_ENUM.WAIT_HANDLE.value]: 'orange',
      [TICKET_STATUS_ENUM.HANDLING.value]: 'blue',
      [TICKET_STATUS_ENUM.REPLIED.value]: 'green',
      [TICKET_STATUS_ENUM.CLOSED.value]: 'default',
    };
    return map[status] || 'default';
  }

  function priorityColor(priority) {
    const map = { 1: 'default', 2: 'blue', 3: 'orange', 4: 'red' };
    return map[priority] || 'default';
  }

  async function showModal(ticketId) {
    visible.value = true;
    loading.value = true;
    try {
      const res = await ticketApi.detail(ticketId);
      detail.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  function onClose() {
    visible.value = false;
    detail.value = null;
  }

  function showReply() {
    replyForm.value = { ticketId: detail.value.ticketId, content: '' };
    replyVisible.value = true;
  }

  async function submitReply() {
    try {
      await replyFormRef.value.validate();
    } catch {
      return;
    }
    replyLoading.value = true;
    try {
      SmartLoading.show();
      await ticketApi.reply(replyForm.value);
      message.success('回复成功');
      replyVisible.value = false;
      // 刷新详情
      const res = await ticketApi.detail(detail.value.ticketId);
      detail.value = res.data;
      emit('reloadList');
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
      replyLoading.value = false;
    }
  }

  defineExpose({ showModal });
</script>

<style scoped>
  .ticket-content {
    padding: 12px;
    background: #fafafa;
    border-radius: 6px;
    white-space: pre-wrap;
    line-height: 1.8;
  }
  .message-list {
    max-height: 400px;
    overflow-y: auto;
  }
  .message-item {
    padding: 10px 12px;
    margin-bottom: 8px;
    border-radius: 6px;
    border-left: 3px solid #d9d9d9;
  }
  .message-customer {
    background: #f6f6f6;
    border-left-color: #1890ff;
  }
  .message-service {
    background: #e6f7ff;
    border-left-color: #52c41a;
  }
  .message-system {
    background: #fffbe6;
    border-left-color: #faad14;
  }
  .message-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }
  .message-name {
    font-weight: 600;
  }
  .message-time {
    margin-left: auto;
    color: #999;
    font-size: 12px;
  }
  .message-content {
    white-space: pre-wrap;
    line-height: 1.6;
  }
</style>