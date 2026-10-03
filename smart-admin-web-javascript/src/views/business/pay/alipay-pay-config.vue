<!--
  * 支付宝商户配置
-->
<template>
  <a-card size="small" :bordered="false" title="支付宝配置">
    <a-alert
      v-if="config.mock"
      type="warning"
      show-icon
      message="当前是演示模式：不会请求支付宝，仅用于后台页面联调。"
      class="smart-margin-bottom10"
    />
    <a-alert
      v-else
      type="info"
      show-icon
      message="商户密钥写在后端 sa-base.yaml 的 alipay.pay。异步通知必须是公网 HTTPS 且不带参数，生产形如 https://desire.wang/api/pay/alipay/notify。"
      class="smart-margin-bottom10"
    />

    <a-descriptions bordered :column="1" size="small" :loading="loading">
      <a-descriptions-item label="启用状态">
        <a-tag :color="config.enabled ? 'green' : 'default'">{{ config.enabled ? '已启用' : '未启用' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="运行模式">
        <a-tag :color="config.mock ? 'orange' : 'green'">{{ config.mock ? '演示数据' : '正式商户' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="环境">
        <a-tag :color="config.sandbox ? 'orange' : 'green'">{{ config.sandbox ? '沙箱' : '生产' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="加签模式">{{ config.signMode || '-' }}</a-descriptions-item>
      <a-descriptions-item label="商户资料">
        <a-tag :color="config.configured ? 'green' : 'orange'">{{ config.configured ? '已填写完整' : '未填写完整' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="AppId">{{ config.appId || '-' }}</a-descriptions-item>
      <a-descriptions-item label="网关">{{ config.gatewayUrl || '-' }}</a-descriptions-item>
      <a-descriptions-item label="回调地址">{{ config.notifyUrl || '-' }}</a-descriptions-item>
      <a-descriptions-item label="应用私钥">
        <a-tag :color="config.privateKeyReady ? 'green' : 'orange'">{{ config.privateKeyReady ? '已配置' : '未配置' }}</a-tag>
      </a-descriptions-item>
    </a-descriptions>

    <a-divider />

    <h3>配置说明</h3>
    <p>密钥不要写进前端或数据库。沙箱联调把 <code>enabled</code> 改为 <code>true</code>，<code>sandbox</code> 保持 <code>true</code>，<code>sign-mode</code> 用 <code>public-key</code>。</p>
    <pre class="pay-config-pre">alipay:
  pay:
    enabled: false
    mock: false
    sandbox: true
    sign-mode: public-key
    notify-url: https://desire.wang/api/pay/alipay/notify
    return-url: https://desire.wang/pay/alipay/return</pre>
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const DEMO_CONFIG = {
    enabled: false,
    mock: false,
    sandbox: true,
    signMode: 'public-key',
    configured: false,
    appId: '',
    gatewayUrl: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
    notifyUrl: 'https://desire.wang/api/pay/alipay/notify',
    privateKeyReady: false,
  };

  const loading = ref(false);
  const config = reactive({ ...DEMO_CONFIG });

  async function loadConfig() {
    loading.value = true;
    try {
      const res = await payApi.getAlipayConfig();
      if (res.data) {
        Object.assign(config, res.data);
      } else {
        Object.assign(config, DEMO_CONFIG);
      }
    } catch (e) {
      Object.assign(config, DEMO_CONFIG);
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
