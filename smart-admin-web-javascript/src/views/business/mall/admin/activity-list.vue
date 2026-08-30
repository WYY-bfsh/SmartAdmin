<template>
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="商品" class="smart-query-form-item">
        <a-input v-model:value="queryForm.goodsName" style="width: 200px" placeholder="商品名称" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button type="primary" @click="onSearch">查询</a-button>
      </a-form-item>
    </a-row>
  </a-form>
  <a-card size="small" :bordered="false">
    <a-alert type="info" show-icon class="smart-margin-bottom10" :message="configTip" />
    <a-row class="smart-table-btn-block">
      <a-button type="primary" @click="showEdit()" v-privilege="'mall:activity:save'">新建活动</a-button>
      <a-button style="margin-left: 8px" @click="openH5">打开用户端 H5</a-button>
    </a-row>
    <a-table size="small" :loading="tableLoading" :dataSource="tableData" :columns="columns" rowKey="activityId" bordered :pagination="false">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'coverUrl'">
          <img v-if="record.coverUrl" :src="record.coverUrl" style="width: 48px; height: 48px; object-fit: cover" />
        </template>
        <template v-if="column.dataIndex === 'saleStatusDesc'">
          <a-tag :color="record.saleStatus === 20 ? 'red' : record.saleStatus === 10 ? 'orange' : 'default'">{{ record.saleStatusDesc }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" @click="showEdit(record)" v-privilege="'mall:activity:save'">编辑</a-button>
        </template>
      </template>
    </a-table>
  </a-card>
  <a-modal :open="visible" :title="form.activityId ? '编辑活动' : '新建活动'" :width="640" @ok="onSubmit" @cancel="visible = false">
    <a-form :label-col="{ span: 6 }">
      <a-form-item label="标题"><a-input v-model:value="form.title" /></a-form-item>
      <a-form-item label="商品名称"><a-input v-model:value="form.goodsName" /></a-form-item>
      <a-form-item label="封面图"><a-input v-model:value="form.coverUrl" placeholder="图片 URL" /></a-form-item>
      <a-form-item label="原价"><a-input-number v-model:value="form.originPrice" :min="0.01" :precision="2" style="width: 100%" /></a-form-item>
      <a-form-item label="秒杀价"><a-input-number v-model:value="form.seckillPrice" :min="0.01" :precision="2" style="width: 100%" /></a-form-item>
      <a-form-item label="库存"><a-input-number v-model:value="form.stock" :min="0" style="width: 100%" /></a-form-item>
      <a-form-item label="每人限购"><a-input-number v-model:value="form.perLimit" :min="1" style="width: 100%" /></a-form-item>
      <a-form-item label="同时抢购人数">
        <a-input-number v-model:value="form.concurrentLimit" :min="1" style="width: 100%" placeholder="空则用全局待定常数" />
      </a-form-item>
      <a-form-item label="分销比例">
        <a-input-number v-model:value="form.commissionRate" :min="0" :max="1" :step="0.01" :precision="4" style="width: 100%" />
      </a-form-item>
      <a-form-item label="开始时间"><a-date-picker show-time v-model:value="form.startTime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" /></a-form-item>
      <a-form-item label="结束时间"><a-date-picker show-time v-model:value="form.endTime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" /></a-form-item>
      <a-form-item label="详情"><a-textarea v-model:value="form.detail" :rows="3" /></a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { mallAdminApi } from '/@/api/business/mall/mall-admin-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const columns = [
    { title: '封面', dataIndex: 'coverUrl', width: 70 },
    { title: '标题', dataIndex: 'title' },
    { title: '商品', dataIndex: 'goodsName' },
    { title: '秒杀价', dataIndex: 'seckillPrice', width: 90 },
    { title: '库存', dataIndex: 'stock', width: 70 },
    { title: '限购', dataIndex: 'perLimit', width: 70 },
    { title: '同时人数', dataIndex: 'concurrentLimit', width: 90 },
    { title: '状态', dataIndex: 'saleStatusDesc', width: 90 },
    { title: '操作', dataIndex: 'action', width: 80 },
  ];
  const queryForm = reactive({ goodsName: '', pageNum: 1, pageSize: 20 });
  const tableData = ref([]);
  const tableLoading = ref(false);
  const visible = ref(false);
  const form = reactive({});
  const configTip = ref('用户端 H5：http://localhost:5173/ ；演示买家 13800000002 / 123456');

  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await mallAdminApi.queryActivity(queryForm);
      tableData.value = res.data?.list || [];
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
  function showEdit(record) {
    Object.assign(form, record || { title: '', goodsName: '', originPrice: 99, seckillPrice: 9.9, stock: 100, perLimit: 1, commissionRate: 0.05, enabledFlag: true });
    visible.value = true;
  }
  async function onSubmit() {
    try {
      await mallAdminApi.saveActivity({ ...form, enabledFlag: true });
      message.success('已保存');
      visible.value = false;
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
  function openH5() {
    const host = location.hostname === 'localhost' || location.hostname === '127.0.0.1' ? 'localhost' : location.hostname;
    window.open(`http://${host}:5173/`, '_blank');
  }
  onMounted(async () => {
    try {
      const res = await mallAdminApi.config();
      configTip.value = `同时抢购人数上限 ${res.data.concurrentLimit}（待定可改 yaml）。支付超时 ${res.data.payTimeoutMinutes} 分钟。一级分销 ${res.data.defaultCommissionRate}。快递100 ${res.data.kuaidi100Enabled ? '已配置真查询' : '未配置，发货后按演示轨迹推进'}。H5：http://localhost:5173/  买家 13800000002 / 123456`;
    } catch (e) {
      smartSentry.captureError(e);
    }
    queryData();
  });
</script>
