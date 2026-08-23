<!--
  * 客服信息查询 - 聚合展示公共信息、个人信息、业务数据
-->
<template>
  <div class="info-query-page">
    <a-row :gutter="16">
      <!-- 左侧：公共信息 -->
      <a-col :span="12">
        <a-card title="系统公告" size="small" :bordered="false" class="info-card">
          <a-spin :spinning="noticeLoading">
            <a-list size="small" :dataSource="noticeList">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <a-tag color="blue" size="small">公告</a-tag>
                      {{ item.title }}
                    </template>
                    <template #description>{{ item.createTime }}</template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-if="!noticeLoading && noticeList.length === 0" description="暂无公告" />
          </a-spin>
        </a-card>

        <a-card title="帮助文档" size="small" :bordered="false" class="info-card" style="margin-top: 16px">
          <a-spin :spinning="helpDocLoading">
            <a-list size="small" :dataSource="helpDocList">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <BookOutlined style="margin-right: 6px" />
                      {{ item.title }}
                    </template>
                    <template #description>{{ item.updateTime }}</template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-if="!helpDocLoading && helpDocList.length === 0" description="暂无帮助文档" />
          </a-spin>
        </a-card>
      </a-col>

      <!-- 右侧：个人信息 + 业务数据 -->
      <a-col :span="12">
        <a-card title="个人信息" size="small" :bordered="false" class="info-card">
          <a-descriptions :column="1" size="small" bordered v-if="userInfo">
            <a-descriptions-item label="员工姓名">{{ userInfo.actualName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="登录账号">{{ userInfo.loginName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="所属部门">{{ userInfo.departmentName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</a-descriptions-item>
            <a-descriptions-item label="上次登录IP">{{ userInfo.lastLoginIp || '-' }}</a-descriptions-item>
            <a-descriptions-item label="上次登录时间">{{ userInfo.lastLoginTime || '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-empty v-else description="未登录或无法获取个人信息" />
        </a-card>

        <a-card title="业务数据概览" size="small" :bordered="false" class="info-card" style="margin-top: 16px">
          <a-spin :spinning="bizLoading">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-statistic title="企业总数" :value="bizData.enterpriseCount" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="公告总数" :value="bizData.noticeCount" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="工单总数" :value="bizData.ticketCount" />
              </a-col>
            </a-row>
            <a-divider />
            <a-row :gutter="16">
              <a-col :span="8">
                <a-statistic title="商品总数" :value="bizData.goodsCount" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="支付订单" :value="bizData.payOrderCount" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="知识库文章" :value="bizData.knowledgeCount" />
              </a-col>
            </a-row>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { BookOutlined } from '@ant-design/icons-vue';
  import { noticeApi } from '/@/api/business/oa/notice-api';
  import { knowledgeApi } from '/@/api/business/customer/knowledge-api';
  import { ticketApi } from '/@/api/business/customer/ticket-api';
  import { useUserStore } from '/@/store/modules/system/user';
  import { smartSentry } from '/@/lib/smart-sentry';

  const userStore = useUserStore();

  const noticeLoading = ref(false);
  const noticeList = ref([]);
  const helpDocLoading = ref(false);
  const helpDocList = ref([]);
  const bizLoading = ref(false);
  const userInfo = ref(null);

  const bizData = reactive({
    enterpriseCount: 0,
    noticeCount: 0,
    ticketCount: 0,
    goodsCount: 0,
    payOrderCount: 0,
    knowledgeCount: 0,
  });

  onMounted(async () => {
    // 获取个人信息
    userInfo.value = {
      actualName: userStore.actualName,
      loginName: userStore.loginName,
      departmentName: userStore.departmentName,
      phone: userStore.phone,
      lastLoginIp: userStore.lastLoginIp,
      lastLoginTime: userStore.lastLoginTime,
    };

    // 获取公告
    noticeLoading.value = true;
    try {
      const res = await noticeApi.queryNoticeEmployee({ pageNum: 1, pageSize: 5 });
      noticeList.value = res.data?.list || [];
      bizData.noticeCount = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      noticeLoading.value = false;
    }

    // 获取知识库
    helpDocLoading.value = true;
    try {
      const res = await knowledgeApi.query({ pageNum: 1, pageSize: 5 });
      helpDocList.value = res.data?.list || [];
      bizData.knowledgeCount = res.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      helpDocLoading.value = false;
    }

    // 获取业务数据统计
    bizLoading.value = true;
    try {
      const ticketRes = await ticketApi.query({ pageNum: 1, pageSize: 1 });
      bizData.ticketCount = ticketRes.data?.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      bizLoading.value = false;
    }
  });
</script>

<style scoped>
  .info-query-page {
    padding: 0;
  }
  .info-card {
    min-height: 200px;
  }
</style>