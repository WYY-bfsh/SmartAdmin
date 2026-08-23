<!--
  * 客服工单管理
-->
<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row" v-privilege="'cs:ticket:query'">
      <a-form-item label="工单编号" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.ticketNo" placeholder="工单编号" />
      </a-form-item>
      <a-form-item label="工单标题" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.title" placeholder="工单标题" />
      </a-form-item>
      <a-form-item label="工单类型" class="smart-query-form-item">
        <SmartEnumSelect enum-name="TICKET_TYPE_ENUM" v-model:value="queryForm.ticketType" width="160px" />
      </a-form-item>
      <a-form-item label="状态" class="smart-query-form-item">
        <SmartEnumSelect enum-name="TICKET_STATUS_ENUM" v-model:value="queryForm.status" width="160px" />
      </a-form-item>
      <a-form-item label="创建日期" class="smart-query-form-item">
        <a-range-picker v-model:value="createTimeRange" style="width: 240px" @change="onDateChange" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="onSearch" v-privilege="'cs:ticket:query'">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="resetQuery" v-privilege="'cs:ticket:query'">
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
        <a-button type="primary" @click="showCreate" v-privilege="'cs:ticket:create'">
          <template #icon><PlusOutlined /></template>
          创建工单
        </a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.BUSINESS.CUSTOMER.TICKET" :refresh="queryData" />
      </div>
    </a-row>

    <a-table
      size="small"
      :loading="tableLoading"
      :dataSource="tableData"
      :columns="columns"
      rowKey="ticketId"
      bordered
      :pagination="false"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'ticketType'">
          {{ $smartEnumPlugin.getDescByValue('TICKET_TYPE_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'priority'">
          <a-tag :color="priorityColor(record.priority)">{{ $smartEnumPlugin.getDescByValue('TICKET_PRIORITY_ENUM', text) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusColor(record.status)">{{ $smartEnumPlugin.getDescByValue('TICKET_STATUS_ENUM', text) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button type="link" @click="showDetail(record)" v-privilege="'cs:ticket:query'">详情</a-button>
            <a-button
              type="link"
              v-if="record.status !== TICKET_STATUS_ENUM.CLOSED.value"
              @click="confirmClose(record)"
              v-privilege="'cs:ticket:close'"
            >
              关闭
            </a-button>
            <a-button type="link" danger @click="confirmDelete(record)" v-privilege="'cs:ticket:delete'">删除</a-button>
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

  <TicketCreateModal ref="createModalRef" @reloadList="queryData" />
  <TicketDetailDrawer ref="detailDrawerRef" @reloadList="queryData" />
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { Modal, message } from 'ant-design-vue';
  import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { ticketApi } from '/@/api/business/customer/ticket-api';
  import { TICKET_STATUS_ENUM } from '/@/constants/business/customer/customer-const';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import TicketCreateModal from './components/ticket-create-modal.vue';
  import TicketDetailDrawer from './components/ticket-detail-drawer.vue';

  const columns = ref([
    { title: '工单编号', dataIndex: 'ticketNo', width: 200, ellipsis: true },
    { title: '标题', dataIndex: 'title', ellipsis: true },
    { title: '类型', dataIndex: 'ticketType', width: 100 },
    { title: '优先级', dataIndex: 'priority', width: 80 },
    { title: '状态', dataIndex: 'status', width: 90 },
    { title: '联系人', dataIndex: 'contactName', width: 100 },
    { title: '联系电话', dataIndex: 'contactPhone', width: 130 },
    { title: '回复数', dataIndex: 'replyCount', width: 80 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 180 },
  ]);

  const queryFormState = {
    ticketNo: '',
    title: '',
    ticketType: undefined,
    status: undefined,
    createTimeBegin: undefined,
    createTimeEnd: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const createTimeRange = ref();
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);
  const createModalRef = ref();
  const detailDrawerRef = ref();

  function priorityColor(priority) {
    const map = { 1: 'default', 2: 'blue', 3: 'orange', 4: 'red' };
    return map[priority] || 'default';
  }

  function statusColor(status) {
    const map = {
      [TICKET_STATUS_ENUM.WAIT_HANDLE.value]: 'orange',
      [TICKET_STATUS_ENUM.HANDLING.value]: 'blue',
      [TICKET_STATUS_ENUM.REPLIED.value]: 'green',
      [TICKET_STATUS_ENUM.CLOSED.value]: 'default',
    };
    return map[status] || 'default';
  }

  function onDateChange(dates) {
    if (dates && dates.length === 2) {
      queryForm.createTimeBegin = dates[0].format('YYYY-MM-DD');
      queryForm.createTimeEnd = dates[1].format('YYYY-MM-DD');
    } else {
      queryForm.createTimeBegin = undefined;
      queryForm.createTimeEnd = undefined;
    }
  }

  function resetQuery() {
    Object.assign(queryForm, { ...queryFormState });
    createTimeRange.value = undefined;
    queryData();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await ticketApi.query(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  function showCreate() {
    createModalRef.value.showModal();
  }

  function showDetail(record) {
    detailDrawerRef.value.showModal(record.ticketId);
  }

  function confirmClose(record) {
    Modal.confirm({
      title: '关闭工单',
      content: `确定关闭工单 ${record.ticketNo} 吗？`,
      okText: '关闭',
      okType: 'danger',
      onOk: async () => {
        try {
          SmartLoading.show();
          await ticketApi.close(record.ticketId);
          message.success('工单已关闭');
          queryData();
        } catch (e) {
          smartSentry.captureError(e);
        } finally {
          SmartLoading.hide();
        }
      },
    });
  }

  function confirmDelete(record) {
    Modal.confirm({
      title: '删除工单',
      content: `确定删除工单 ${record.ticketNo} 吗？删除后不可恢复。`,
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        try {
          SmartLoading.show();
          await ticketApi.delete(record.ticketId);
          message.success('工单已删除');
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