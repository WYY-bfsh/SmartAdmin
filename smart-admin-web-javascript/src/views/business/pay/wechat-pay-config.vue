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
      message="商户密钥写在后端 sa-base.yaml 的 wechat.pay。生产回调必须是 https://desire.wang/api/pay/wechat/notify（经 Nginx /api 转到 Java）。秒杀商城启用后支持 Native 扫码、微信内 JSAPI、手机 H5；未启用则仍走收款码截图。"
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
      <a-descriptions-item label="H5 app_url">{{ config.h5AppUrl || '-' }}</a-descriptions-item>
      <a-descriptions-item label="商户私钥">
        <a-tag :color="config.privateKeyReady ? 'green' : 'orange'">{{ config.privateKeyReady ? '已配置' : '未配置' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="AppSecret（JSAPI）">
        <a-tag :color="config.appSecretReady ? 'green' : 'orange'">{{ config.appSecretReady ? '已配置' : '未配置' }}</a-tag>
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
    <p>正式环境把 <code>enabled</code> 改为 <code>true</code>、<code>mock</code> 改为 <code>false</code>，填写 AppId、AppSecret（微信内支付授权用）、商户号、APIv3 密钥、证书序列号、私钥，并设置：</p>
    <pre class="pay-config-pre">notify-url: https://desire.wang/api/pay/wechat/notify
h5-app-url: https://desire.wang</pre>
    <p>微信商户平台同时配置该回调地址。填完后重启 Java，秒杀待付款页会自动走微信支付。</p>
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
