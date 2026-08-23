<!--
  * 微信支付商户配置
-->
<template>
  <a-card size="small" :bordered="false" title="微信支付商户配置">
    <a-alert
      type="info"
      show-icon
      message="商户密钥请写在后端 sa-base.yaml 的 wechat.pay 节点，不要放到前端或数据库。本地调试时 notify-url 必须是公网 HTTPS，可用内网穿透。"
      class="smart-margin-bottom10"
    />

    <a-descriptions bordered :column="1" size="small" :loading="loading">
      <a-descriptions-item label="启用状态">
        <a-tag :color="config.enabled ? 'green' : 'default'">{{ config.enabled ? '已启用' : '未启用' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="商户资料">
        <a-tag :color="config.configured ? 'green' : 'orange'">{{ config.configured ? '已填写完整' : '未填写完整' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="AppId">{{ config.appId || '-' }}</a-descriptions-item>
      <a-descriptions-item label="商户号">{{ config.mchId || '-' }}</a-descriptions-item>
      <a-descriptions-item label="回调地址">{{ config.notifyUrl || '-' }}</a-descriptions-item>
      <a-descriptions-item label="商户私钥">
        <a-tag :color="config.privateKeyReady ? 'green' : 'orange'">{{ config.privateKeyReady ? '已配置' : '未配置' }}</a-tag>
      </a-descriptions-item>
    </a-descriptions>

    <a-divider />

    <h3>填写说明</h3>
    <p>打开后端文件：<code>smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml</code></p>
    <pre class="pay-config-pre">wechat:
  pay:
    enabled: true
    app-id: 你的AppId
    mch-id: 你的商户号
    api-v3-key: 32位APIv3密钥
    merchant-serial-number: 商户证书序列号
    private-key-path: D:/cert/apiclient_key.pem
    notify-url: https://你的域名/pay/wechat/notify</pre>
    <p>证书和密钥在微信商户平台「账户中心 → API安全」下载。修改后重启后端。</p>
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const loading = ref(false);
  const config = reactive({
    enabled: false,
    configured: false,
    appId: '',
    mchId: '',
    notifyUrl: '',
    privateKeyReady: false,
  });

  async function loadConfig() {
    loading.value = true;
    try {
      const res = await payApi.getConfig();
      Object.assign(config, res.data);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  onMounted(loadConfig);
</script>

<style scoped>
  .pay-config-pre {
    background: #f6f6f6;
    padding: 12px 16px;
    border-radius: 4px;
    overflow: auto;
  }
</style>
