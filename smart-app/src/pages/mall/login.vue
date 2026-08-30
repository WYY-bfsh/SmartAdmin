<template>
  <view class="login">
    <view class="hero">
      <view class="name">抢购商城</view>
      <view class="sub">{{ isRegister ? '注册账号，开启秒杀' : '欢迎登录' }}</view>
    </view>
    <view class="card">
      <view class="label">手机号</view>
      <input class="input" type="number" maxlength="11" v-model="form.phone" placeholder="请输入手机号" />
      <view class="label">密码</view>
      <input class="input" password v-model="form.password" placeholder="请输入密码" />
      <template v-if="isRegister">
        <view class="label">邀请码（选填）</view>
        <input class="input" v-model="form.inviteCode" placeholder="上级邀请码" />
      </template>
      <view v-if="apiHint" class="err">{{ apiHint }}</view>
      <button class="btn" :disabled="loading" @click="submit">{{ loading ? '请稍候…' : isRegister ? '注册' : '登录' }}</button>
      <view class="switch" @click="isRegister = !isRegister">{{ isRegister ? '已有账号，去登录' : '没有账号，去注册' }}</view>
    </view>
    <view class="demo">
      <view class="demo-title">点一下填入演示账号</view>
      <view class="chip" @click="fill('13800000002')">下级买家 13800000002 / 123456</view>
      <view class="chip" @click="fill('13800000001')">上级店长 13800000001 / 123456</view>
    </view>
  </view>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import { mallH5Api, saveMallToken } from '@/api/business/mall/mall-h5-api';
  import { MALL_INVITE } from '@/constants/local-storage-key-const';
  import { SmartToast } from '@/lib/smart-support';

  const isRegister = ref(false);
  const loading = ref(false);
  const apiHint = ref('');
  const form = reactive({
    phone: '',
    password: '',
    inviteCode: uni.getStorageSync(MALL_INVITE) || '',
  });

  function fill(phone) {
    form.phone = phone;
    form.password = '123456';
    isRegister.value = false;
  }

  async function submit() {
    if (!form.phone || !form.password) {
      SmartToast.toast('请输入手机号和密码');
      return;
    }
    loading.value = true;
    apiHint.value = '';
    try {
      const res = isRegister.value ? await mallH5Api.register(form) : await mallH5Api.login(form);
      saveMallToken(res.data.token);
      SmartToast.success('登录成功');
      uni.switchTab({ url: '/pages/mall/home' });
    } catch (e) {
      const msg = (e && e.msg) || '';
      if (String(msg).includes('No static resource') || String(msg).includes('mall/h5') || (e && e.code === 10001)) {
        apiHint.value = '后端还是旧进程，秒杀接口没加载。请停止 Java 后重新运行 AdminApplication。';
      }
    } finally {
      loading.value = false;
    }
  }

  onShow(async () => {
    try {
      await mallH5Api.config();
    } catch (e) {
      apiHint.value = '连不上秒杀接口。请确认已重新启动 Java 后端（端口 1024）。';
    }
  });
</script>

<style lang="scss" scoped>
  .login {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .hero {
    background: linear-gradient(135deg, #ee0a24, #ff6a3d);
    color: #fff;
    padding: 80rpx 48rpx 96rpx;
  }
  .name {
    font-size: 56rpx;
    font-weight: 800;
  }
  .sub {
    margin-top: 12rpx;
    opacity: 0.9;
  }
  .card {
    margin: -48rpx 32rpx 0;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 32rpx 32rpx;
  }
  .label {
    font-size: 24rpx;
    color: #888;
    margin: 20rpx 0 8rpx;
  }
  .input {
    height: 80rpx;
    border-bottom: 1px solid #eee;
    font-size: 32rpx;
  }
  .btn {
    margin-top: 48rpx;
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 44rpx;
  }
  .switch {
    text-align: center;
    color: #1677ff;
    margin-top: 28rpx;
    font-size: 26rpx;
  }
  .err {
    margin-top: 20rpx;
    color: #ee0a24;
    font-size: 24rpx;
    line-height: 1.5;
  }
  .demo {
    margin: 40rpx 32rpx;
    font-size: 24rpx;
    color: #888;
  }
  .demo-title {
    margin-bottom: 16rpx;
  }
  .chip {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    color: #333;
  }
</style>
