<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="订单号" class="smart-query-form-item">
        <a-input v-model:value="queryForm.orderNo" style="width: 200px" />
      </a-form-item>
      <a-form-item label="状态" class="smart-query-form-item">
        <SmartEnumSelect enum-name="MALL_ORDER_STATUS_ENUM" v-model:value="queryForm.orderStatus" width="140px" />
      </a-form-item>
      <a-form-item label="运单号" class="smart-query-form-item">
        <a-input v-model:value="queryForm.waybillNo" style="width: 180px" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button type="primary" @click="onSearch">查询</a-button>
      </a-form-item>
    </a-row>
  </a-form>
  <a-card size="small" :bordered="false">
    <a-table size="small" :loading="tableLoading" :dataSource="tableData" :columns="columns" rowKey="orderId" bordered :pagination="false">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'orderStatus'">
          <a-tag>{{ $smartEnumPlugin.getDescByValue('MALL_ORDER_STATUS_ENUM', record.orderStatus) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" v-if="record.orderStatus === 20" @click="openShip(record)" v-privilege="'mall:order:ship'">发货</a-button>
          <a-button type="link" @click="openDetail(record)">物流</a-button>
        </template>
      </template>
    </a-table>
    <div class="smart-query-table-page">
      <a-pagination v-model:current="queryForm.pageNum" v-model:pageSize="queryForm.pageSize" :total="total" @change="queryData" show-size-changer />
    </div>
  </a-card>
  <a-modal :open="shipVisible" title="真实发货" ok-text="确认发货" @ok="doShip" @cancel="shipVisible = false">
    <p>填写快递公司与运单号，买家即可按快递100结构查看轨迹。</p>
    <a-form :label-col="{ span: 6 }">
      <a-form-item label="快递公司">
        <a-select v-model:value="shipForm.expressCode" :options="companies" :field-names="{ label: 'name', value: 'code' }" />
      </a-form-item>
      <a-form-item label="运单号">
        <a-input v-model:value="shipForm.waybillNo" placeholder="真实运单号" />
        <a-button type="link" @click="shipForm.waybillNo = 'SF' + Date.now().toString().slice(-12)">生成演示运单</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
  <a-modal :open="traceVisible" title="物流轨迹" :footer="null" @cancel="traceVisible = false">
    <a-timeline>
      <a-timeline-item v-for="(item, idx) in traces" :key="idx">
        <div>{{ item.ftime }} {{ item.statusText }}</div>
        <div>{{ item.context }}</div>
      </a-timeline-item>
    </a-timeline>
    <a-empty v-if="!traces.length" description="尚未发货" />
  </a-modal>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { mallAdminApi } from '/@/api/business/mall/mall-admin-api';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import { smartSentry } from '/@/lib/smart-sentry';

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', width: 220 },
    { title: '商品', dataIndex: 'goodsName' },
    { title: '金额', dataIndex: 'amount', width: 90 },
    { title: '收货人', dataIndex: 'receiverName', width: 90 },
    { title: '电话', dataIndex: 'receiverPhone', width: 120 },
    { title: '快递', dataIndex: 'expressName', width: 100 },
    { title: '运单号', dataIndex: 'waybillNo', width: 160 },
    { title: '状态', dataIndex: 'orderStatus', width: 90 },
    { title: '操作', dataIndex: 'action', width: 140 },
  ];
  const queryForm = reactive({ orderNo: '', orderStatus: undefined, waybillNo: '', pageNum: 1, pageSize: 10 });
  const tableData = ref([]);
  const total = ref(0);
  const tableLoading = ref(false);
  const shipVisible = ref(false);
  const traceVisible = ref(false);
  const traces = ref([]);
  const companies = ref([]);
  const shipForm = reactive({ orderId: undefined, expressCode: 'shunfeng', waybillNo: '' });

  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await mallAdminApi.queryOrder(queryForm);
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
  function openShip(record) {
    shipForm.orderId = record.orderId;
    shipForm.expressCode = 'shunfeng';
    shipForm.waybillNo = '';
    shipVisible.value = true;
  }
  async function doShip() {
    try {
      await mallAdminApi.ship(shipForm);
      message.success('已发货');
      shipVisible.value = false;
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
  async function openDetail(record) {
    try {
      const res = await mallAdminApi.orderDetail(record.orderId);
      traces.value = res.data?.traces || [];
      traceVisible.value = true;
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
  onMounted(async () => {
    try {
      const res = await mallAdminApi.companies();
      companies.value = res.data || [];
    } catch (e) {
      smartSentry.captureError(e);
    }
    queryData();
  });
</script>
