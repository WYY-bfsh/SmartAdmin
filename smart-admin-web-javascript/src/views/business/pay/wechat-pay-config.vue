<!--
  * 微信支付商户配置
-->
<template>
  <a-card size="small" :bordered="false" title="微信支付商户配置">
    <a-alert
      v-if="config.mock"
      type="warning"
      show-icon
      message="当前是演示模式：没有真实微信商户号，系统已填入示例数据。可以下单、看收款码、模拟支付和退款，但不会向微信收款。"
      class="smart-margin-bottom10"
    />
    <a-alert
      v-else
      type="info"
      show-icon
      message="商户密钥请写在后端 sa-base.yaml 的 wechat.pay 节点，不要放到前端或数据库。本地调试时 notify-url 必须是公网 HTTPS，可用内网穿透。"
      class="smart-margin-bottom10"
    />

    <a-descriptions bordered :column="1" size="small" :loading="loading">
      <a-descriptions-item label="启用状态">
        <a-tag :color="config.enabled ? 'green' : 'default'">{{ config.enabled ? '已启用' : '未启用' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="运行模式">
        <a-tag :color="config.mock ? 'orange' : 'green'">{{ config.mock ? '演示数据' : '正式商户' }}</a-tag>
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

    <h3>演示数据说明</h3>
    <p>没有商户号时，开发环境已自动使用下面这套数据（写在 <code>sa-base.yaml</code>）：</p>
    <pre class="pay-config-pre">wechat:
  pay:
    enabled: true
    mock: true
    app-id: wx8f3a2c1d4e5b6789
    mch-id: 1639284750
    api-v3-key: SmartAdminWxPayDemoKey32Chars!!
    merchant-serial-number: 5A8C2E1B9D4F60783C1A0E6B2D9F4C7A8E1B3D5F
    private-key: DEMO_MOCK_PRIVATE_KEY
    notify-url: https://demo.smartadmin.local/pay/wechat/notify</pre>
    <p>以后有真实商户，把 <code>mock</code> 改成 <code>false</code>，再换成微信商户平台里的 AppId、商户号、密钥和证书，然后重启后端。</p>
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { payApi } from '/@/api/business/pay/pay-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const DEMO_CONFIG = {
    enabled: true,
    mock: true,
    configured: true,
    appId: 'wx8f3a2c1d4e5b6789',
    mchId: '1639284750',
    notifyUrl: 'https://demo.smartadmin.local/pay/wechat/notify',
    privateKeyReady: true,
  };

  const loading = ref(false);
  const config = reactive({ ...DEMO_CONFIG });

  async function loadConfig() {
    loading.value = true;
    try {
      const res = await payApi.getConfig();
      if (res.data && (res.data.mchId || res.data.mock)) {
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
