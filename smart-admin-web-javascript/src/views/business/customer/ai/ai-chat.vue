<!--
  * 客服 AI 问答
-->
<template>
  <a-card size="small" :bordered="false" class="ai-chat-card">
    <div class="ai-chat-list" ref="listRef">
      <div v-if="messages.length === 0" class="ai-chat-empty">
        <div>直接输入问题。回答会参考知识库和已回答的问答。</div>
        <div class="ai-chat-presets">
          <a-button v-for="item in presets" :key="item" size="small" @click="askPreset(item)">{{ item }}</a-button>
        </div>
      </div>
      <div v-for="(item, index) in messages" :key="index" :class="['ai-chat-row', item.role]">
        <div class="ai-chat-bubble">{{ item.text }}</div>
      </div>
    </div>
    <div class="ai-chat-input">
      <a-textarea
        v-model:value="draft"
        :auto-size="{ minRows: 2, maxRows: 6 }"
        placeholder="输入问题，Enter 发送，Shift+Enter 换行"
        @pressEnter="onPressEnter"
      />
      <a-button type="primary" :loading="sending" :disabled="!draft.trim()" @click="send">发送</a-button>
    </div>
  </a-card>
</template>

<script setup>
  import { nextTick, onMounted, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import LocalStorageKeyConst from '/@/constants/local-storage-key-const';
  import { localRead } from '/@/utils/local-util';
  import { aiChatApi } from '/@/api/business/customer/ai-chat-api';

  const presets = ['怎么提交工单？', '订单支付后多久发货？', '如何查看佣金？'];
  const messages = ref([]);
  const draft = ref('');
  const sending = ref(false);
  const topicId = ref('');
  const listRef = ref();

  onMounted(async () => {
    try {
      const res = await aiChatApi.getHistory();
      const saved = res?.data?.content;
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed.messages)) {
          messages.value = parsed.messages;
        }
        if (parsed.topicId) {
          topicId.value = parsed.topicId;
        }
      }
    } catch (e) {
      messages.value = [];
    }
    scrollBottom();
  });

  function askPreset(text) {
    draft.value = text;
    send();
  }

  function onPressEnter(event) {
    if (event.shiftKey) {
      return;
    }
    event.preventDefault();
    send();
  }

  async function send() {
    const text = draft.value.trim();
    if (!text || sending.value) {
      return;
    }
    messages.value.push({ role: 'user', text });
    const answer = { role: 'assistant', text: '' };
    messages.value.push(answer);
    draft.value = '';
    sending.value = true;
    scrollBottom();
    try {
      const token = localRead(LocalStorageKeyConst.USER_TOKEN);
      const response = await fetch(`${import.meta.env.VITE_APP_API_URL}/customer/ai/chat/send`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token ? `Bearer ${token}` : '',
        },
        body: JSON.stringify({ topicId: topicId.value, message: text, useContext: true }),
      });
      if (!response.ok || !response.body) {
        answer.text = '发送失败，请稍后再试。';
        return;
      }
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      while (true) {
        const { value, done } = await reader.read();
        if (done) {
          break;
        }
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        for (const line of lines) {
          const data = line.trim().startsWith('data:') ? line.trim().slice(5).trim() : '';
          if (!data || data === '[DONE]') {
            continue;
          }
          try {
            const json = JSON.parse(data);
            if (json.topicId) {
              topicId.value = json.topicId;
            }
            if (json.content) {
              answer.text += json.content;
              scrollBottom();
            }
          } catch (e) {
            // 忽略非 JSON 的心跳行
          }
        }
      }
      if (!answer.text) {
        answer.text = '没有收到回答。';
      }
      persist();
    } catch (e) {
      answer.text = answer.text || '网络中断，请稍后再试。';
      message.error('AI 问答请求失败');
    } finally {
      sending.value = false;
    }
  }

  function persist() {
    aiChatApi.saveHistory(JSON.stringify({ topicId: topicId.value, messages: messages.value })).catch(() => {});
  }

  function scrollBottom() {
    nextTick(() => {
      if (listRef.value) {
        listRef.value.scrollTop = listRef.value.scrollHeight;
      }
    });
  }
</script>

<style scoped>
  .ai-chat-card {
    min-height: 640px;
  }
  .ai-chat-list {
    height: 520px;
    overflow-y: auto;
    padding: 12px;
    background: #f7f8fa;
  }
  .ai-chat-empty {
    color: #8c8c8c;
    text-align: center;
    padding-top: 160px;
  }
  .ai-chat-presets {
    display: flex;
    justify-content: center;
    gap: 8px;
    flex-wrap: wrap;
    margin-top: 16px;
  }
  .ai-chat-row {
    display: flex;
    margin-bottom: 12px;
  }
  .ai-chat-row.user {
    justify-content: flex-end;
  }
  .ai-chat-bubble {
    max-width: 70%;
    white-space: pre-wrap;
    padding: 10px 14px;
    border-radius: 8px;
    background: #fff;
  }
  .ai-chat-row.user .ai-chat-bubble {
    background: #1677ff;
    color: #fff;
  }
  .ai-chat-input {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    margin-top: 12px;
  }
  .ai-chat-input :deep(textarea) {
    flex: 1;
  }
</style>
