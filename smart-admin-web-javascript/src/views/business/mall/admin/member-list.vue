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
    <a-table size="small" :dataSource="tableData" :columns="columns" rowKey="memberId" bordered :pagination="false" :loading="tableLoading">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'avatar'">
          <a-image v-if="record.avatar" :src="fileUrl(record.avatar)" :width="40" :height="40" style="object-fit: cover; border-radius: 20px" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'wechatPayQr'">
          <a-image v-if="record.wechatPayQr" :src="fileUrl(record.wechatPayQr)" :width="48" :height="48" style="object-fit: contain" />
          <span v-else>未上传</span>
        </template>
        <template v-else-if="column.dataIndex === 'wechatReceiveQr'">
          <a-image v-if="record.wechatReceiveQr" :src="fileUrl(record.wechatReceiveQr)" :width="48" :height="48" style="object-fit: contain" />
          <span v-else>未上传</span>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-button type="link" @click="showDetail(record)">查看</a-button>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal :open="visible" title="客户信息" :width="720" :footer="null" @cancel="visible = false">
    <a-descriptions :column="2" size="small" bordered>
      <a-descriptions-item label="手机号">{{ current.phone }}</a-descriptions-item>
      <a-descriptions-item label="昵称">{{ current.nickname }}</a-descriptions-item>
      <a-descriptions-item label="邀请码">{{ current.inviteCode }}</a-descriptions-item>
      <a-descriptions-item label="团队人数">{{ current.teamCount }}</a-descriptions-item>
      <a-descriptions-item label="待结算">{{ current.frozenCommission }}</a-descriptions-item>
      <a-descriptions-item label="已结算">{{ current.settledCommission }}</a-descriptions-item>
      <a-descriptions-item label="注册时间" :span="2">{{ current.createTime }}</a-descriptions-item>
    </a-descriptions>

    <a-row :gutter="16" class="qr-block">
      <a-col :span="8">
        <div class="qr-title">头像</div>
        <a-image v-if="current.avatar" :src="fileUrl(current.avatar)" :width="140" :height="140" style="object-fit: cover" />
        <div v-else class="empty">未上传</div>
        <div class="ops">
          <a-upload :show-upload-list="false" :before-upload="beforeUpload" :custom-request="(opt) => uploadField('avatar', opt)">
            <a-button size="small">更换</a-button>
          </a-upload>
          <a-button size="small" :disabled="!current.avatar" @click="download(fileUrl(current.avatar), '头像')">下载</a-button>
          <a-button size="small" danger :disabled="!current.avatar" @click="clearField('avatar')">清空</a-button>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="qr-title">微信支付码</div>
        <a-image v-if="current.wechatPayQr" :src="fileUrl(current.wechatPayQr)" :width="140" :height="140" style="object-fit: contain" />
        <div v-else class="empty">未上传</div>
        <div class="ops">
          <a-upload :show-upload-list="false" :before-upload="beforeUpload" :custom-request="(opt) => uploadField('wechatPayQr', opt)">
            <a-button size="small">更换</a-button>
          </a-upload>
          <a-button size="small" :disabled="!current.wechatPayQr" @click="download(fileUrl(current.wechatPayQr), '微信支付码')">下载</a-button>
          <a-button size="small" danger :disabled="!current.wechatPayQr" @click="clearField('wechatPayQr')">清空</a-button>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="qr-title">支付宝收款码</div>
        <a-image v-if="current.wechatReceiveQr" :src="fileUrl(current.wechatReceiveQr)" :width="140" :height="140" style="object-fit: contain" />
        <div v-else class="empty">未上传</div>
        <div class="ops">
          <a-upload :show-upload-list="false" :before-upload="beforeUpload" :custom-request="(opt) => uploadField('wechatReceiveQr', opt)">
            <a-button size="small">更换</a-button>
          </a-upload>
          <a-button size="small" :disabled="!current.wechatReceiveQr" @click="download(fileUrl(current.wechatReceiveQr), '支付宝收款码')">下载</a-button>
          <a-button size="small" danger :disabled="!current.wechatReceiveQr" @click="clearField('wechatReceiveQr')">清空</a-button>
        </div>
      </a-col>
    </a-row>
  </a-modal>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { mallAdminApi } from '/@/api/business/mall/mall-admin-api';
  import { fileApi } from '/@/api/support/file-api';
  import { FILE_FOLDER_TYPE_ENUM } from '/@/constants/support/file-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { resolveUploadUrl } from '/@/utils/mall-public-url';

  const columns = [
    { title: '头像', dataIndex: 'avatar', width: 70 },
    { title: '手机号', dataIndex: 'phone' },
    { title: '昵称', dataIndex: 'nickname' },
    { title: '微信支付码', dataIndex: 'wechatPayQr', width: 110 },
    { title: '支付宝收款码', dataIndex: 'wechatReceiveQr', width: 110 },
    { title: '邀请码', dataIndex: 'inviteCode' },
    { title: '团队人数', dataIndex: 'teamCount', width: 90 },
    { title: '注册时间', dataIndex: 'createTime' },
    { title: '操作', dataIndex: 'action', width: 80 },
  ];
  const queryForm = reactive({ phone: '', pageNum: 1, pageSize: 20 });
  const tableData = ref([]);
  const tableLoading = ref(false);
  const visible = ref(false);
  const current = reactive({});

  function fileUrl(url) {
    return resolveUploadUrl(url);
  }

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

  function showDetail(record) {
    Object.assign(current, record);
    visible.value = true;
  }

  function beforeUpload(file) {
    const ok = /\.(png|jpe?g|gif|webp)$/i.test(file.name);
    if (!ok) {
      message.error('请上传图片');
      return false;
    }
    if (file.size / 1024 / 1024 > 5) {
      message.error('图片需小于 5MB');
      return false;
    }
    return true;
  }

  async function saveField(field, value) {
    await mallAdminApi.updateMember({
      memberId: current.memberId,
      [field]: value,
    });
    current[field] = value;
    const row = tableData.value.find((e) => e.memberId === current.memberId);
    if (row) {
      row[field] = value;
    }
  }

  async function uploadField(field, options) {
    try {
      const formData = new FormData();
      formData.append('file', options.file);
      const res = await fileApi.uploadFile(formData, FILE_FOLDER_TYPE_ENUM.MEDIA.value);
      const url = res.data?.fileUrl || '';
      await saveField(field, url);
      message.success('已更新');
      options.onSuccess && options.onSuccess();
    } catch (e) {
      options.onError && options.onError(e);
      smartSentry.captureError(e);
    }
  }

  async function clearField(field) {
    try {
      await saveField(field, '');
      message.success('已清空');
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  function download(url, name) {
    if (!url) {
      return;
    }
    const a = document.createElement('a');
    a.href = url;
    a.target = '_blank';
    a.download = `${name || '图片'}.png`;
    a.click();
  }

  onMounted(queryData);
</script>
<style scoped>
  .qr-block {
    margin-top: 20px;
  }
  .qr-title {
    font-weight: 600;
    margin-bottom: 8px;
  }
  .empty {
    width: 140px;
    height: 140px;
    background: #fafafa;
    border: 1px dashed #ddd;
    color: #999;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .ops {
    margin-top: 8px;
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
</style>
