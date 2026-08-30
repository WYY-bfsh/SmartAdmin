<template>
  <a-card size="small" :bordered="false">
    <a-alert type="info" show-icon class="smart-margin-bottom10" message="一级分销：下级用邀请码注册，付款后佣金冻结，买家确认收货后结算给上级。不是拉人头资金盘。" />
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="订单号" class="smart-query-form-item">
          <a-input v-model:value="queryForm.orderNo" style="width: 200px" />
        </a-form-item>
        <a-form-item label="状态" class="smart-query-form-item">
          <SmartEnumSelect enum-name="COMMISSION_STATUS_ENUM" v-model:value="queryForm.status" width="140px" />
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button type="primary" @click="queryData">查询</a-button>
        </a-form-item>
      </a-row>
    </a-form>
    <a-table size="small" :dataSource="tableData" :columns="columns" rowKey="commissionId" bordered :pagination="false" :loading="tableLoading">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'status'">
          {{ $smartEnumPlugin.getDescByValue('COMMISSION_STATUS_ENUM', record.status) }}
        </template>
      </template>
    </a-table>
  </a-card>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { mallAdminApi } from '/@/api/business/mall/mall-admin-api';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import { smartSentry } from '/@/lib/smart-sentry';

  const columns = [
    { title: '订单号', dataIndex: 'orderNo' },
    { title: '受益人', dataIndex: 'memberPhone' },
    { title: '下单人', dataIndex: 'fromMemberPhone' },
    { title: '比例', dataIndex: 'rate', width: 90 },
    { title: '金额', dataIndex: 'amount', width: 90 },
    { title: '状态', dataIndex: 'status', width: 90 },
    { title: '结算时间', dataIndex: 'settleTime' },
  ];
  const queryForm = reactive({ orderNo: '', status: undefined, pageNum: 1, pageSize: 20 });
  const tableData = ref([]);
  const tableLoading = ref(false);
  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await mallAdminApi.queryCommission(queryForm);
      tableData.value = res.data?.list || [];
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }
  onMounted(queryData);
</script>
