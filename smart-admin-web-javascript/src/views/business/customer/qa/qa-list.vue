<!--
  * 问答管理 - 一问一答
-->
<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row" v-privilege="'cs:qa:query'">
      <a-form-item label="搜索" class="smart-query-form-item">
        <a-input style="width: 220px" v-model:value="queryForm.question" placeholder="搜索提问内容" />
      </a-form-item>
      <a-form-item label="状态" class="smart-query-form-item">
        <SmartEnumSelect enum-name="QA_STATUS_ENUM" v-model:value="queryForm.status" width="160px" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="onSearch" v-privilege="'cs:qa:query'">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="resetQuery" v-privilege="'cs:qa:query'">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-button-group>
      </a-form-item>
    </a-row>
  </a-form>

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-row class="smart-table-btn-block">
      <div class="smart-table-operate-block">
        <a-button type="primary" @click="showAsk" v-privilege="'cs:qa:ask'">
          <template #icon><QuestionCircleOutlined /></template>
          我要提问
        </a-button>
      </div>
    </a-row>

    <a-spin :spinning="tableLoading">
      <div class="qa-list">
        <div v-for="item in tableData" :key="item.qaId" class="qa-card">
          <!-- 问题 -->
          <div class="qa-question">
            <span class="qa-badge q">问</span>
            <div class="qa-body">
              <div class="qa-text">{{ item.question }}</div>
              <div class="qa-meta">
                <span>{{ item.askerName || '匿名用户' }}</span>
                <span>{{ item.createTime }}</span>
                <a-tag :color="qaStatusColor(item.status)" size="small">{{ $smartEnumPlugin.getDescByValue('QA_STATUS_ENUM', item.status) }}</a-tag>
              </div>
            </div>
          </div>

          <!-- 回答 -->
          <div class="qa-answer" v-if="item.status === QA_STATUS_ENUM.ANSWERED.value && item.answer">
            <span class="qa-badge a">答</span>
            <div class="qa-body">
              <div class="qa-text">{{ item.answer }}</div>
              <div class="qa-meta">
                <span>客服</span>
                <span>{{ item.answerTime }}</span>
              </div>
            </div>
          </div>

          <!-- 驳回 -->
          <div class="qa-answer qa-rejected" v-if="item.status === QA_STATUS_ENUM.REJECTED.value && item.answer">
            <span class="qa-badge r">驳</span>
            <div class="qa-body">
              <div class="qa-text">{{ item.answer }}</div>
              <div class="qa-meta">
                <span>客服</span>
                <span>{{ item.answerTime }}</span>
              </div>
            </div>
          </div>

          <!-- 操作 -->
          <div class="qa-actions">
            <a-button
              v-if="item.status === QA_STATUS_ENUM.WAIT_ANSWER.value"
              type="link"
              size="small"
              @click="showAnswer(item)"
              v-privilege="'cs:qa:answer'"
            >
              回答
            </a-button>
            <a-button
              v-if="item.status === QA_STATUS_ENUM.WAIT_ANSWER.value"
              type="link"
              size="small"
              danger
              @click="showReject(item)"
              v-privilege="'cs:qa:answer'"
            >
              驳回
            </a-button>
            <a-button type="link" size="small" danger @click="confirmDelete(item)" v-privilege="'cs:qa:delete'">删除</a-button>
          </div>
        </div>
      </div>
    </a-spin>

    <div class="smart-query-table-page" v-if="total > 0">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :defaultPageSize="queryForm.pageSize"
        v-model:current="queryForm.pageNum"
        v-model:pageSize="queryForm.pageSize"
        :total="total"
        @change="queryData"
        :show-total="(total) => `共${total}条`"
      />
    </div>

    <a-empty v-if="!tableLoading && tableData.length === 0" description="暂无问答，快来提问吧" />
  </a-card>

  <!-- 提问弹窗 -->
  <a-modal :title="'我要提问'" :open="askVisible" :confirmLoading="askLoading" @ok="submitAsk" @cancel="askVisible = false" width="500px">
    <a-form ref="askFormRef" :model="askForm" :rules="askRules">
      <a-form-item label="提问内容" name="question">
        <a-textarea v-model:value="askForm.question" placeholder="请输入您的问题" :rows="4" :maxlength="500" showCount />
      </a-form-item>
      <a-form-item label="您的称呼" name="askerName">
        <a-input v-model:value="askForm.askerName" placeholder="请输入称呼" :maxlength="50" />
      </a-form-item>
      <a-form-item label="联系电话" name="askerPhone">
        <a-input v-model:value="askForm.askerPhone" placeholder="选填" :maxlength="20" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- 回答弹窗 -->
  <a-modal :title="answerTitle" :open="answerVisible" :confirmLoading="answerLoading" @ok="submitAnswer" @cancel="answerVisible = false" width="500px">
    <a-form ref="answerFormRef" :model="answerForm" :rules="answerRules">
      <a-form-item :label="answerLabel" name="answer">
        <a-textarea v-model:value="answerForm.answer" placeholder="请输入回答内容" :rows="4" :maxlength="2000" showCount />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { Modal, message } from 'ant-design-vue';
  import { QuestionCircleOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { qaApi } from '/@/api/business/customer/qa-api';
  import { QA_STATUS_ENUM } from '/@/constants/business/customer/customer-const';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

  const queryFormState = { question: '', status: undefined, pageNum: 1, pageSize: 10 };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  // 提问
  const askVisible = ref(false);
  const askLoading = ref(false);
  const askFormRef = ref();
  const askForm = ref({ question: '', askerName: '', askerPhone: '' });
  const askRules = { question: [{ required: true, message: '请输入提问内容' }] };

  // 回答/驳回
  const answerVisible = ref(false);
  const answerLoading = ref(false);
  const answerFormRef = ref();
  const answerForm = ref({ qaId: null, answer: '', action: 'answer' });
  const answerTitle = ref('');
  const answerLabel = ref('');
  const answerRules = { answer: [] };

  function qaStatusColor(status) {
    const map = { [QA_STATUS_ENUM.WAIT_ANSWER.value]: 'orange', [QA_STATUS_ENUM.ANSWERED.value]: 'green', [QA_STATUS_ENUM.REJECTED.value]: 'red' };
    return map[status] || 'default';
  }

  function resetQuery() {
    Object.assign(queryForm, { ...queryFormState });
    queryData();
  }
  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await qaApi.query(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  function showAsk() {
    askForm.value = { question: '', askerName: '', askerPhone: '' };
    askVisible.value = true;
  }

  async function submitAsk() {
    try { await askFormRef.value.validate(); } catch { return; }
    askLoading.value = true;
    try {
      SmartLoading.show();
      await qaApi.ask(askForm.value);
      message.success('提问已提交');
      askVisible.value = false;
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
      askLoading.value = false;
    }
  }

  function showAnswer(item) {
    answerForm.value = { qaId: item.qaId, answer: '', action: 'answer' };
    answerTitle.value = '回答';
    answerLabel.value = '回答内容';
    answerRules.value = { answer: [{ required: true, message: '请输入回答内容' }] };
    answerVisible.value = true;
  }

  function showReject(item) {
    answerForm.value = { qaId: item.qaId, answer: '', action: 'reject' };
    answerTitle.value = '驳回答';
    answerLabel.value = '驳回原因';
    answerRules.value = { answer: [{ required: true, message: '请输入驳回原因' }] };
    answerVisible.value = true;
  }

  async function submitAnswer() {
    try { await answerFormRef.value.validate(); } catch { return; }
    answerLoading.value = true;
    try {
      SmartLoading.show();
      await qaApi.answer(answerForm.value);
      message.success(answerForm.value.action === 'reject' ? '已驳回' : '已回答');
      answerVisible.value = false;
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
      answerLoading.value = false;
    }
  }

  function confirmDelete(item) {
    Modal.confirm({
      title: '删除问答',
      content: '确定删除这条问答吗？',
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        try {
          SmartLoading.show();
          await qaApi.delete(item.qaId);
          message.success('已删除');
          queryData();
        } catch (e) {
          smartSentry.captureError(e);
        } finally {
          SmartLoading.hide();
        }
      },
    });
  }

  onMounted(queryData);
</script>

<style scoped>
  .qa-list {
    margin-top: 8px;
  }
  .qa-card {
    padding: 16px;
    margin-bottom: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    transition: box-shadow 0.2s;
  }
  .qa-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
  .qa-question, .qa-answer {
    display: flex;
    gap: 12px;
    margin-bottom: 8px;
  }
  .qa-badge {
    flex-shrink: 0;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    color: #fff;
  }
  .qa-badge.q { background: #1890ff; }
  .qa-badge.a { background: #52c41a; }
  .qa-badge.r { background: #ff4d4f; }
  .qa-body { flex: 1; }
  .qa-text {
    line-height: 1.8;
    white-space: pre-wrap;
    margin-bottom: 6px;
  }
  .qa-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #999;
    font-size: 12px;
  }
  .qa-answer {
    margin-left: 40px;
    padding: 12px;
    background: #f6ffed;
    border-radius: 6px;
  }
  .qa-rejected {
    background: #fff2f0;
  }
  .qa-actions {
    margin-left: 40px;
    margin-top: 4px;
  }
</style>