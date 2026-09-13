<template>
  <!-- 未登录访问抢购/订单时弹出，登录成功后留在当前 Tab -->
  <view v-if="modelValue" class="mask" @click="goHome">
    <view class="box" @click.stop>
      <view class="title">请先登录</view>
      <view class="label">手机号</view>
      <input class="input" type="tel" maxlength="11" v-model="form.phone" placeholder="请输入手机号" autocomplete="tel" />
      <view class="label">密码</view>
      <input class="input" password v-model="form.password" placeholder="请输入密码" autocomplete="current-password" />
      <view v-if="apiHint" class="err">{{ apiHint }}</view>
      <button class="btn" :disabled="loading" @click="submit">{{ loading ? '请稍候…' : '登录' }}</button>
      <view class="links">
        <text @click="goRegister">立即注册</text>
        <text @click="goHome">返回首页</text>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { reactive, ref, watch } from 'vue';
  import { mallH5Api, saveMallToken } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const props = defineProps({
    modelValue: { type: Boolean, default: false },
  });
  const emit = defineEmits(['update:modelValue', 'success']);

  const loading = ref(false);
  const apiHint = ref('');
  const form = reactive({
    phone: '',
    password: '',
  });

  watch(
    () => props.modelValue,
    (open) => {
      if (open) {
        apiHint.value = '';
        form.phone = '';
        form.password = '';
      }
    },
  );

  function goHome() {
    emit('update:modelValue', false);
    uni.switchTab({ url: '/pages/mall/index' });
  }

  function goRegister() {
    emit('update:modelValue', false);
    uni.navigateTo({ url: '/pages/mall/register' });
  }

  async function submit() {
    if (!form.phone || !form.password) {
      SmartToast.toast('请输入手机号和密码');
      return;
    }
    loading.value = true;
    apiHint.value = '';
    try {
      const res = await mallH5Api.login(form);
      saveMallToken(res.data.token);
      SmartToast.success('登录成功');
      emit('update:modelValue', false);
      emit('success');
    } catch (e) {
      const msg = (e && e.msg) || '';
      if (String(msg).includes('No static resource') || String(msg).includes('mall/h5') || (e && e.code === 10001)) {
        apiHint.value = '后端还是旧进程，秒杀接口没加载。请停止 Java 后重新运行 AdminApplication。';
      }
    } finally {
      loading.value = false;
    }
  }
</script>

<style lang="scss" scoped>
  .mask {
    position: fixed;
    inset: 0;
    z-index: 999;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48rpx;
    box-sizing: border-box;
  }
  .box {
    width: 100%;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 36rpx 32rpx;
    box-sizing: border-box;
  }
  .title {
    font-size: 36rpx;
    font-weight: 700;
    color: #111;
    text-align: center;
    margin-bottom: 12rpx;
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
    margin-top: 40rpx;
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 44rpx;
  }
  .links {
    display: flex;
    justify-content: space-between;
    color: #1677ff;
    margin-top: 28rpx;
    font-size: 26rpx;
  }
  .err {
    margin-top: 16rpx;
    color: #ee0a24;
    font-size: 24rpx;
    line-height: 1.5;
  }
</style>
