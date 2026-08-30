<template>
  <view class="page">
    <view class="card" v-for="item in list" :key="item.addressId">
      <view>{{ item.receiverName }} {{ item.receiverPhone }}</view>
      <view class="sub">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}</view>
    </view>
    <view class="card">
      <input class="input" v-model="form.receiverName" placeholder="收货人" />
      <input class="input" v-model="form.receiverPhone" placeholder="手机号" />
      <input class="input" v-model="form.province" placeholder="省" />
      <input class="input" v-model="form.city" placeholder="市" />
      <input class="input" v-model="form.district" placeholder="区" />
      <input class="input" v-model="form.detail" placeholder="详细地址" />
      <button class="btn" @click="save">保存为默认地址</button>
    </view>
  </view>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import { mallH5Api, getMallToken } from '@/api/business/mall/mall-h5-api';
  import { SmartToast } from '@/lib/smart-support';

  const list = ref([]);
  const form = reactive({
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    defaultFlag: true,
  });

  async function load() {
    if (!getMallToken()) {
      uni.navigateTo({ url: '/pages/mall/login' });
      return;
    }
    const res = await mallH5Api.addressList();
    list.value = res.data || [];
  }

  async function save() {
    if (!form.receiverName || !form.receiverPhone || !form.detail) {
      SmartToast.toast('请填写收货人、手机号和详细地址');
      return;
    }
    try {
      await mallH5Api.saveAddress(form);
      SmartToast.success('已保存');
      load();
    } catch (e) {
      // toast already shown
    }
  }

  onShow(load);
</script>

<style lang="scss" scoped>
  .page {
    min-height: 100vh;
    background: #f5f5f5;
  }
  .card {
    background: #fff;
    padding: 24rpx 32rpx;
    margin-bottom: 16rpx;
  }
  .sub {
    color: #999;
    font-size: 24rpx;
    margin-top: 8rpx;
  }
  .input {
    height: 80rpx;
    border-bottom: 1px solid #eee;
    font-size: 28rpx;
    margin-bottom: 8rpx;
  }
  .btn {
    margin-top: 24rpx;
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 44rpx;
  }
</style>
