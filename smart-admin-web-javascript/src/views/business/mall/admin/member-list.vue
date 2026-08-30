<template>
  <a-card size="small" :bordered="false">
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="手机号" class="smart-query-form-item">
          <a-input v-model:value="queryForm.phone" style="width: 180px" />
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button type="primary" @click="queryData">查询</a-button>
        </a-form-item>
      </a-row>
    </a-form>
    <a-table size="small" :dataSource="tableData" :columns="columns" rowKey="memberId" bordered :pagination="false" :loading="tableLoading" />
  </a-card>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { mallAdminApi } from '/@/api/business/mall/mall-admin-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const columns = [
    { title: '手机号', dataIndex: 'phone' },
    { title: '昵称', dataIndex: 'nickname' },
    { title: '邀请码', dataIndex: 'inviteCode' },
    { title: '团队人数', dataIndex: 'teamCount' },
    { title: '待结算佣金', dataIndex: 'frozenCommission' },
    { title: '已结算佣金', dataIndex: 'settledCommission' },
    { title: '注册时间', dataIndex: 'createTime' },
  ];
  const queryForm = reactive({ phone: '', pageNum: 1, pageSize: 20 });
  const tableData = ref([]);
  const tableLoading = ref(false);
  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await mallAdminApi.queryMember(queryForm);
      tableData.value = res.data?.list || [];
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }
  onMounted(queryData);
</script>
