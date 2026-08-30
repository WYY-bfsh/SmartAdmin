<!--
  * 微信支付订单
-->
<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row" v-privilege="'pay:order:query'">
      <a-form-item label="商户订单号" class="smart-query-form-item">
        <a-input style="width: 220px" v-model:value="queryForm.orderNo" placeholder="商户订单号" />
      </a-form-item>
      <a-form-item label="支付状态" class="smart-query-form-item">
        <SmartEnumSelect enum-name="PAY_STATUS_ENUM" v-model:value="queryForm.payStatus" width="160px" />
      </a-form-item>
      <a-form-item label="创建日期" class="smart-query-form-item">
        <a-range-picker v-model:value="createTimeRange" style="width: 240px" @change="onDateChange" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="onSearch" v-privilege="'pay:order:query'">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="resetQuery" v-privilege="'pay:order:query'">
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
      v-if="mockMode"
      type="warning"
      show-icon
      class="smart-margin-bottom10"
      message="当前是微信支付演示模式：没有真实商户号，扫码不会向微信收款。创建订单后点「模拟支付」即可走完支付和退款流程。"
    />
    <a-row class="smart-table-btn-block">
      <div class="smart-table-operate-block">
        <a-button type="primary" @click="showCreate()" v-privilege="'pay:order:create'">
          <template #icon>
            <PlusOutlined />
          </template>
          发起支付
        </a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.BUSINESS.PAY.ORDER" :refresh="queryData" />
      </div>
    </a-row>

    <a-table
      size="small"
      :loading="tableLoading"
      :dataSource="tableData"
      :columns="columns"
      rowKey="payOrderId"
      bordered
      :pagination="false"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'payStatus'">
          <a-tag :color="statusColor(record.payStatus)">{{ $smartEnumPlugin.getDescByValue('PAY_STATUS_ENUM', text) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'tradeType'">
          {{ $smartEnumPlugin.getDescByValue('PAY_TRADE_TYPE_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'amountYuan'">
          ¥{{ formatMoney(record.amountYuan) }}
        </template>
        <template v-if="column.dataIndex === 'refundAmountYuan'">
          <span v-if="record.refundAmount">¥{{ formatMoney(record.refundAmountYuan) }}</span>
          <span v-else>-</span>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button type="link" v-if="record.payOrderId > 0 && record.payStatus === PAY_STATUS_ENUM.WAIT_PAY.value" @click="showQrcode(record)" v-privilege="'pay:order:query'">
              二维码
            </a-button>
            <a-button
              type="link"
              v-if="mockMode && record.payOrderId > 0 && record.payStatus === PAY_STATUS_ENUM.WAIT_PAY.value"
              @click="mockPay(record)"
              v-privilege="'pay:order:sync'"
            >
              模拟支付
            </a-button>
            <a-button type="link" v-if="record.payOrderId > 0" @click="syncStatus(record)" v-privilege="'pay:order:sync'">同步</a-button>
            <a-button
              type="link"
              v-if="record.payOrderId > 0 && record.payStatus === PAY_STATUS_ENUM.WAIT_PAY.value"
              @click="confirmClose(record)"
              v-privilege="'pay:order:close'"
            >
              关闭
            </a-button>
            <a-button
              type="link"
              v-if="record.payOrderId > 0 && (record.payStatus === PAY_STATUS_ENUM.SUCCESS.value || record.payStatus === PAY_STATUS_ENUM.REFUNDING.value)"
              @click="showRefund(record)"
              v-privilege="'pay:order:refund'"
            >
              退款
            </a-button>
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

  <PayCreateModal ref="createModalRef" @reloadList="queryData" @showQrcode="onCreated" />
  <PayQrcodeModal ref="qrcodeModalRef" @paid="queryData" />
  <PayRefundModal ref="refundModalRef" @reloadList="queryData" />
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Modal, message } from 'ant-design-vue';
  import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { PAY_STATUS_ENUM } from '/@/constants/business/pay/pay-const';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import PayCreateModal from './components/pay-create-modal.vue';
  import PayQrcodeModal from './components/pay-qrcode-modal.vue';
  import PayRefundModal from './components/pay-refund-modal.vue';

  const columns = ref([
    { title: '商户订单号', dataIndex: 'orderNo', width: 220, ellipsis: true },
    { title: '商品描述', dataIndex: 'description', ellipsis: true },
    { title: '金额', dataIndex: 'amountYuan', width: 100 },
    { title: '支付方式', dataIndex: 'tradeType', width: 100 },
    { title: '状态', dataIndex: 'payStatus', width: 100 },
    { title: '微信单号', dataIndex: 'transactionId', width: 220, ellipsis: true },
    { title: '已退款', dataIndex: 'refundAmountYuan', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 280 },
  ]);

  const queryFormState = {
    orderNo: '',
    payStatus: undefined,
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
  const qrcodeModalRef = ref();
  const refundModalRef = ref();
  const mockMode = ref(true);
  const route = useRoute();
  const router = useRouter();

  function statusColor(status) {
    const map = {
      [PAY_STATUS_ENUM.WAIT_PAY.value]: 'orange',
      [PAY_STATUS_ENUM.SUCCESS.value]: 'green',
      [PAY_STATUS_ENUM.CLOSED.value]: 'default',
      [PAY_STATUS_ENUM.REFUNDING.value]: 'blue',
      [PAY_STATUS_ENUM.REFUND.value]: 'purple',
    };
    return map[status] || 'default';
  }

  function formatMoney(value) {
    if (value === undefined || value === null || value === '') {
      return '0.00';
    }
    return Number(value).toFixed(2);
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

  const DEMO_ORDERS = [
    {
      payOrderId: -1,
      orderNo: 'WX202608221000010001',
      description: '演示-会员月卡',
      amountYuan: 0.01,
      tradeType: 1,
      payStatus: 10,
      transactionId: null,
      refundAmount: 0,
      refundAmountYuan: 0,
      createTime: '2026-08-22 10:00:01',
    },
    {
      payOrderId: -2,
      orderNo: 'WX202608211430220002',
      description: '演示-办公用品采购',
      amountYuan: 128.0,
      tradeType: 1,
      payStatus: 20,
      transactionId: '4200002208261234567890123456',
      refundAmount: 0,
      refundAmountYuan: 0,
      createTime: '2026-08-21 14:30:22',
    },
    {
      payOrderId: -3,
      orderNo: 'WX202608201015330003',
      description: '演示-已关闭订单',
      amountYuan: 9.9,
      tradeType: 1,
      payStatus: 30,
      transactionId: null,
      refundAmount: 0,
      refundAmountYuan: 0,
      createTime: '2026-08-20 10:15:33',
    },
    {
      payOrderId: -4,
      orderNo: 'WX202608191600440004',
      description: '演示-全额退款',
      amountYuan: 66.0,
      tradeType: 1,
      payStatus: 50,
      transactionId: '4200001908261234567890123456',
      refundAmount: 6600,
      refundAmountYuan: 66.0,
      createTime: '2026-08-19 16:00:44',
    },
    {
      payOrderId: -5,
      orderNo: 'WX202608181100550005',
      description: '演示-部分退款',
      amountYuan: 199.0,
      tradeType: 1,
      payStatus: 40,
      transactionId: '4200001808261234567890123456',
      refundAmount: 5000,
      refundAmountYuan: 50.0,
      createTime: '2026-08-18 11:00:55',
    },
  ];

  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await payApi.queryOrder(queryForm);
      const list = res.data?.list || [];
      tableData.value = list.length ? list : DEMO_ORDERS;
      total.value = list.length ? res.data.total : DEMO_ORDERS.length;
    } catch (e) {
      tableData.value = DEMO_ORDERS;
      total.value = DEMO_ORDERS.length;
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  async function loadConfig() {
    try {
      const res = await payApi.getConfig();
      mockMode.value = res.data?.mock !== false;
    } catch (e) {
      mockMode.value = true;
      smartSentry.captureError(e);
    }
  }

  function showCreate(preset) {
    createModalRef.value.showModal(preset);
  }

  async function mockPay(record) {
    try {
      SmartLoading.show();
      await payApi.mockPay(record.payOrderId);
      message.success('已模拟支付成功');
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  function onCreated(payData) {
    queryData();
    qrcodeModalRef.value.showModal(payData);
  }

  function showQrcode(record) {
    qrcodeModalRef.value.showModal(record.payOrderId);
  }

  function showRefund(record) {
    refundModalRef.value.showModal(record);
  }

  async function syncStatus(record) {
    try {
      SmartLoading.show();
      await payApi.sync(record.payOrderId);
      message.success('已同步微信支付状态');
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  function confirmClose(record) {
    Modal.confirm({
      title: '关闭订单',
      content: `确定关闭订单 ${record.orderNo} 吗？关闭后不可再支付。`,
      okText: '关闭',
      okType: 'danger',
      onOk: async () => {
        try {
          SmartLoading.show();
          await payApi.close(record.payOrderId);
          message.success('订单已关闭');
          queryData();
        } catch (e) {
          smartSentry.captureError(e);
        } finally {
          SmartLoading.hide();
        }
      },
    });
  }

  onMounted(async () => {
    await loadConfig();
    await queryData();
    const description = route.query.description;
    const amountYuan = route.query.amountYuan;
    if (description || amountYuan) {
      showCreate({
        description,
        amountYuan,
        remark: route.query.remark,
      });
      router.replace({ path: '/pay/order' });
    }
  });
</script>
