<!--
  * 支付对账
-->
<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row" v-privilege="'pay:recon:query'">
      <a-form-item label="账单日期" class="smart-query-form-item">
        <a-date-picker v-model:value="billDate" style="width: 180px" value-format="YYYY-MM-DD" :disabled-date="disabledBillDate" />
      </a-form-item>
      <a-form-item label="支付渠道" class="smart-query-form-item">
        <SmartEnumSelect enum-name="PAY_CHANNEL_ENUM" v-model:value="queryForm.payChannel" width="160px" />
      </a-form-item>
      <a-form-item label="来源" class="smart-query-form-item">
        <SmartEnumSelect enum-name="PAY_RECON_SOURCE_ENUM" v-model:value="queryForm.sourceType" width="160px" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="onSearch" v-privilege="'pay:recon:query'">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="resetQuery" v-privilege="'pay:recon:query'">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-button-group>
      </a-form-item>
    </a-row>
  </a-form>

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-alert
      type="info"
      show-icon
      class="smart-margin-bottom10"
      message="官方账单为 T+1，请选昨天及更早。按商户订单号匹配金额和状态。支付宝/微信可拉官方账单（需真实商户且账单已出）；也可上传 csv/txt/zip/gz。沙箱或未出账单时用演示对账。"
    />
    <a-row class="smart-table-btn-block" :gutter="8">
      <div class="smart-table-operate-block">
        <a-button type="primary" @click="doPull" v-privilege="'pay:recon:pull'">
          <template #icon><CloudDownloadOutlined /></template>
          拉取官方账单
        </a-button>
        <a-upload :show-upload-list="false" :before-upload="beforeUpload" accept=".csv,.txt,.zip,.gz">
          <a-button v-privilege="'pay:recon:upload'">
            <template #icon><ImportOutlined /></template>
            上传账单
          </a-button>
        </a-upload>
        <a-button @click="doMock" v-privilege="'pay:recon:mock'">演示对账</a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.BUSINESS.PAY.RECON_BATCH" :refresh="queryData" />
      </div>
    </a-row>

    <a-table
      size="small"
      :loading="tableLoading"
      :dataSource="tableData"
      :columns="columns"
      rowKey="batchId"
      bordered
      :pagination="false"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'payChannel'">
          {{ $smartEnumPlugin.getDescByValue('PAY_CHANNEL_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'sourceType'">
          {{ $smartEnumPlugin.getDescByValue('PAY_RECON_SOURCE_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'batchStatus'">
          <a-tag :color="record.batchStatus === 20 ? 'green' : record.batchStatus === 30 ? 'red' : 'orange'">
            {{ $smartEnumPlugin.getDescByValue('PAY_RECON_BATCH_STATUS_ENUM', text) }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'localAmountYuan'">{{ formatMoney(text) }}</template>
        <template v-if="column.dataIndex === 'channelAmountYuan'">{{ formatMoney(text) }}</template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button type="link" @click="openItems(record)" v-privilege="'pay:recon:query'">明细</a-button>
            <a-button type="link" @click="exportItems(record)" v-privilege="'pay:recon:export'">导出</a-button>
            <a-button type="link" danger @click="deleteBatch(record)" v-privilege="'pay:recon:delete'">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
    <div class="smart-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :current="queryForm.pageNum"
        :pageSize="queryForm.pageSize"
        :total="total"
        @change="onPageChange"
        :show-total="(t) => `共${t}条`"
      />
    </div>
  </a-card>

  <a-drawer v-model:open="itemOpen" title="对账明细" width="1100" destroy-on-close>
    <a-descriptions v-if="currentBatch" size="small" bordered :column="3" class="smart-margin-bottom10">
      <a-descriptions-item label="账单日">{{ currentBatch.billDate }}</a-descriptions-item>
      <a-descriptions-item label="匹配">{{ currentBatch.matchedCount }}</a-descriptions-item>
      <a-descriptions-item label="金额不符">{{ currentBatch.amountDiffCount }}</a-descriptions-item>
      <a-descriptions-item label="状态不符">{{ currentBatch.statusDiffCount }}</a-descriptions-item>
      <a-descriptions-item label="仅本地">{{ currentBatch.localOnlyCount }}</a-descriptions-item>
      <a-descriptions-item label="仅渠道">{{ currentBatch.channelOnlyCount }}</a-descriptions-item>
    </a-descriptions>
    <a-form layout="inline" class="smart-margin-bottom10">
      <a-form-item label="结果">
        <SmartEnumSelect enum-name="PAY_RECON_MATCH_STATUS_ENUM" v-model:value="itemQuery.matchStatus" width="160px" />
      </a-form-item>
      <a-form-item label="订单号">
        <a-input v-model:value="itemQuery.orderNo" allow-clear style="width: 200px" />
      </a-form-item>
      <a-form-item label="核销">
        <a-select v-model:value="itemQuery.handledFlag" allow-clear placeholder="全部" style="width: 120px">
          <a-select-option :value="false">未核销</a-select-option>
          <a-select-option :value="true">已核销</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="queryItems">筛选</a-button>
        <a-button class="smart-margin-left10" @click="exportItems(currentBatch)" v-privilege="'pay:recon:export'">导出Excel</a-button>
      </a-form-item>
    </a-form>
    <a-table size="small" :loading="itemLoading" :dataSource="itemData" :columns="itemColumns" rowKey="itemId" bordered :pagination="false">
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'matchStatus'">
          <a-tag :color="matchColor(record.matchStatus)">
            {{ $smartEnumPlugin.getDescByValue('PAY_RECON_MATCH_STATUS_ENUM', text) }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'bizType'">
          {{ $smartEnumPlugin.getDescByValue('PAY_RECON_BIZ_TYPE_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'localStatus'">
          {{ $smartEnumPlugin.getDescByValue('PAY_STATUS_ENUM', text) || '-' }}
        </template>
        <template v-if="column.dataIndex === 'localAmountYuan'">{{ formatMoney(text) }}</template>
        <template v-if="column.dataIndex === 'channelAmountYuan'">{{ formatMoney(text) }}</template>
        <template v-if="column.dataIndex === 'action'">
          <a-button
            type="link"
            v-if="!record.handledFlag && record.matchStatus !== 10"
            @click="handleItem(record)"
            v-privilege="'pay:recon:handle'"
          >
            核销
          </a-button>
          <span v-else-if="record.handledFlag">已核销</span>
        </template>
      </template>
    </a-table>
    <div class="smart-query-table-page">
      <a-pagination
        showSizeChanger
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :current="itemQuery.pageNum"
        :pageSize="itemQuery.pageSize"
        :total="itemTotal"
        @change="onItemPageChange"
        :show-total="(t) => `共${t}条`"
      />
    </div>
  </a-drawer>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import dayjs from 'dayjs';
  import { message, Modal } from 'ant-design-vue';
  import { CloudDownloadOutlined, ImportOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { PAY_RECON_MATCH_STATUS_ENUM } from '/@/constants/business/pay/pay-const';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import TableOperator from '/@/components/support/table-operator/index.vue';

  const columns = ref([
    { title: '账单日', dataIndex: 'billDate', width: 120 },
    { title: '渠道', dataIndex: 'payChannel', width: 90 },
    { title: '来源', dataIndex: 'sourceType', width: 100 },
    { title: '状态', dataIndex: 'batchStatus', width: 90 },
    { title: '本地笔数', dataIndex: 'localCount', width: 90 },
    { title: '渠道笔数', dataIndex: 'channelCount', width: 90 },
    { title: '匹配', dataIndex: 'matchedCount', width: 70 },
    { title: '金额不符', dataIndex: 'amountDiffCount', width: 90 },
    { title: '仅本地', dataIndex: 'localOnlyCount', width: 80 },
    { title: '仅渠道', dataIndex: 'channelOnlyCount', width: 80 },
    { title: '本地金额', dataIndex: 'localAmountYuan', width: 100 },
    { title: '渠道金额', dataIndex: 'channelAmountYuan', width: 100 },
    { title: '说明', dataIndex: 'remark', ellipsis: true },
    { title: '失败原因', dataIndex: 'errorMsg', ellipsis: true, width: 180 },
    { title: '操作', dataIndex: 'action', width: 180, fixed: 'right' },
  ]);

  const itemColumns = [
    { title: '结果', dataIndex: 'matchStatus', width: 100 },
    { title: '类型', dataIndex: 'bizType', width: 70 },
    { title: '商户订单号', dataIndex: 'orderNo', width: 200, ellipsis: true },
    { title: '本地金额', dataIndex: 'localAmountYuan', width: 90 },
    { title: '本地状态', dataIndex: 'localStatus', width: 90 },
    { title: '渠道金额', dataIndex: 'channelAmountYuan', width: 90 },
    { title: '渠道状态', dataIndex: 'channelStatus', width: 90 },
    { title: '渠道单号', dataIndex: 'channelTradeNo', width: 180, ellipsis: true },
    { title: '说明', dataIndex: 'remark', ellipsis: true },
    { title: '操作', dataIndex: 'action', width: 80 },
  ];

  const queryFormState = {
    billDate: undefined,
    payChannel: undefined,
    sourceType: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const billDate = ref(dayjs().subtract(1, 'day').format('YYYY-MM-DD'));
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  const itemOpen = ref(false);
  const itemLoading = ref(false);
  const itemData = ref([]);
  const itemTotal = ref(0);
  const currentBatch = ref(null);
  const itemQuery = reactive({
    batchId: undefined,
    matchStatus: undefined,
    orderNo: '',
    handledFlag: undefined,
    pageNum: 1,
    pageSize: 10,
  });

  function disabledBillDate(current) {
    return current && current > dayjs().endOf('day');
  }

  function formatMoney(value) {
    if (value === undefined || value === null || value === '') {
      return '-';
    }
    return Number(value).toFixed(2);
  }

  function matchColor(status) {
    const map = {
      [PAY_RECON_MATCH_STATUS_ENUM.MATCHED.value]: 'green',
      [PAY_RECON_MATCH_STATUS_ENUM.AMOUNT_DIFF.value]: 'red',
      [PAY_RECON_MATCH_STATUS_ENUM.STATUS_DIFF.value]: 'orange',
      [PAY_RECON_MATCH_STATUS_ENUM.LOCAL_ONLY.value]: 'purple',
      [PAY_RECON_MATCH_STATUS_ENUM.CHANNEL_ONLY.value]: 'blue',
    };
    return map[status] || 'default';
  }

  function requireDateChannel() {
    if (!billDate.value) {
      message.warning('请先选账单日期');
      return false;
    }
    if (!queryForm.payChannel) {
      message.warning('请先选支付渠道');
      return false;
    }
    return true;
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      queryForm.billDate = billDate.value || undefined;
      const res = await payApi.queryReconBatch(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  function resetQuery() {
    Object.assign(queryForm, { ...queryFormState });
    billDate.value = dayjs().subtract(1, 'day').format('YYYY-MM-DD');
    queryData();
  }

  function onPageChange(page, pageSize) {
    queryForm.pageNum = page;
    queryForm.pageSize = pageSize;
    queryData();
  }

  async function doPull() {
    if (!requireDateChannel()) {
      return;
    }
    Modal.confirm({
      title: '拉取官方账单并对账',
      content: '官方账单为 T+1。确定按所选日期和渠道向微信/支付宝拉账单？',
      onOk: async () => {
        try {
          SmartLoading.show();
          const res = await payApi.pullRecon({ billDate: billDate.value, payChannel: queryForm.payChannel });
          message.success(res.data?.remark || '对账完成');
          queryData();
          if (res.data?.batchId) {
            openItems(res.data);
          }
        } catch (e) {
          queryData();
          smartSentry.captureError(e);
        } finally {
          SmartLoading.hide();
        }
      },
    });
  }

  async function doMock() {
    if (!requireDateChannel()) {
      return;
    }
    try {
      SmartLoading.show();
      const res = await payApi.mockRecon({ billDate: billDate.value, payChannel: queryForm.payChannel });
      message.success(res.data?.remark || '演示对账完成');
      queryData();
      if (res.data?.batchId) {
        openItems(res.data);
      }
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  function beforeUpload(file) {
    if (!requireDateChannel()) {
      return false;
    }
    const formData = new FormData();
    formData.append('file', file);
    formData.append('payChannel', queryForm.payChannel);
    formData.append('billDate', billDate.value);
    SmartLoading.show();
    payApi
      .uploadRecon(formData)
      .then((res) => {
        message.success(res.data?.remark || '上传对账完成');
        queryData();
        if (res.data?.batchId) {
          openItems(res.data);
        }
      })
      .catch((e) => smartSentry.captureError(e))
      .finally(() => SmartLoading.hide());
    return false;
  }

  function openItems(record) {
    currentBatch.value = record;
    itemQuery.batchId = record.batchId;
    itemQuery.matchStatus = undefined;
    itemQuery.orderNo = '';
    itemQuery.handledFlag = undefined;
    itemQuery.pageNum = 1;
    itemOpen.value = true;
    queryItems();
  }

  async function queryItems() {
    if (!itemQuery.batchId) {
      return;
    }
    itemLoading.value = true;
    try {
      const res = await payApi.queryReconItem(itemQuery);
      itemData.value = res.data?.list || [];
      itemTotal.value = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      itemLoading.value = false;
    }
  }

  function onItemPageChange(page, pageSize) {
    itemQuery.pageNum = page;
    itemQuery.pageSize = pageSize;
    queryItems();
  }

  function handleItem(record) {
    Modal.confirm({
      title: '核销这条差异',
      content: '只标记已人工核对，不会改支付单或渠道账单。',
      onOk: async () => {
        await payApi.handleRecon({ itemId: record.itemId, remark: '人工核销' });
        message.success('已核销');
        queryItems();
      },
    });
  }

  function exportItems(record) {
    if (!record?.batchId) {
      return;
    }
    payApi.exportReconItem(record.batchId);
  }

  function deleteBatch(record) {
    Modal.confirm({
      title: '删除对账批次',
      content: '将删除该批次及其明细，不可恢复。',
      onOk: async () => {
        await payApi.deleteReconBatch(record.batchId);
        message.success('已删除');
        if (currentBatch.value?.batchId === record.batchId) {
          itemOpen.value = false;
        }
        queryData();
      },
    });
  }

  onMounted(queryData);
</script>
