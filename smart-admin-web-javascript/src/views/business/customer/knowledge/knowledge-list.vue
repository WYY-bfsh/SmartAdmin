<!--
  * 知识库管理
-->
<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row" v-privilege="'cs:knowledge:query'">
      <a-form-item label="标题" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.title" placeholder="标题关键词" />
      </a-form-item>
      <a-form-item label="分类" class="smart-query-form-item">
        <SmartEnumSelect enum-name="KNOWLEDGE_CATEGORY_ENUM" v-model:value="queryForm.category" width="160px" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="onSearch" v-privilege="'cs:knowledge:query'">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="resetQuery" v-privilege="'cs:knowledge:query'">
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
        <a-button type="primary" @click="showAdd" v-privilege="'cs:knowledge:edit'">
          <template #icon><PlusOutlined /></template>
          新增知识
        </a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.BUSINESS.CUSTOMER.KNOWLEDGE" :refresh="queryData" />
      </div>
    </a-row>

    <a-table
      size="small"
      :loading="tableLoading"
      :dataSource="tableData"
      :columns="columns"
      rowKey="knowledgeId"
      bordered
      :pagination="false"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'category'">
          {{ $smartEnumPlugin.getDescByValue('KNOWLEDGE_CATEGORY_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'title'">
          <a @click="showDetail(record)" v-privilege="'cs:knowledge:query'">{{ text }}</a>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button type="link" @click="showDetail(record)" v-privilege="'cs:knowledge:query'">查看</a-button>
            <a-button type="link" @click="showEdit(record)" v-privilege="'cs:knowledge:edit'">编辑</a-button>
            <a-button type="link" danger @click="confirmDelete(record)" v-privilege="'cs:knowledge:edit'">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>

    <div class="smart-query-table-page">
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
  </a-card>

  <KnowledgeFormModal ref="formModalRef" @reloadList="queryData" />
  <a-modal
    :title="detailTitle"
    :open="detailVisible"
    @cancel="detailVisible = false"
    :footer="null"
    width="800px"
  >
    <a-spin :spinning="detailLoading">
      <template v-if="detailContent">
        <div class="knowledge-detail" v-html="detailContent"></div>
      </template>
    </a-spin>
  </a-modal>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { Modal, message } from 'ant-design-vue';
  import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { knowledgeApi } from '/@/api/business/customer/knowledge-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import KnowledgeFormModal from './components/knowledge-form-modal.vue';

  const columns = ref([
    { title: '标题', dataIndex: 'title', ellipsis: true },
    { title: '分类', dataIndex: 'category', width: 120 },
    { title: '排序', dataIndex: 'sort', width: 80 },
    { title: '浏览数', dataIndex: 'viewCount', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 180 },
  ]);

  const queryFormState = {
    title: '',
    category: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);
  const formModalRef = ref();
  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const detailTitle = ref('');
  const detailContent = ref('');

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
      const res = await knowledgeApi.query(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  function showAdd() {
    formModalRef.value.showModal();
  }

  function showEdit(record) {
    formModalRef.value.showModal(record.knowledgeId);
  }

  async function showDetail(record) {
    detailVisible.value = true;
    detailLoading.value = true;
    try {
      const res = await knowledgeApi.detail(record.knowledgeId);
      detailTitle.value = res.data.title;
      detailContent.value = res.data.content || '<p>暂无内容</p>';
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      detailLoading.value = false;
    }
  }

  function confirmDelete(record) {
    Modal.confirm({
      title: '删除知识',
      content: `确定删除"${record.title}"吗？`,
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        try {
          SmartLoading.show();
          await knowledgeApi.delete(record.knowledgeId);
          message.success('删除成功');
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
  .knowledge-detail {
    padding: 16px;
    line-height: 1.8;
    max-height: 60vh;
    overflow-y: auto;
  }
  .knowledge-detail :deep(img) {
    max-width: 100%;
  }
</style>