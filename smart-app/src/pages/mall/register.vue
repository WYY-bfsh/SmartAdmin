<template>
  <view class="register">
    <view class="title">注册</view>

    <view class="avatar-wrap" @click="chooseAvatar">
      <image v-if="avatarPreview" class="avatar" :src="avatarPreview" mode="aspectFill" />
      <view v-else class="avatar empty">+</view>
      <view class="avatar-tip">{{ uploadingField === 'avatar' ? '上传中…' : '点击上传头像' }}</view>
    </view>

    <view class="form">
      <input class="input" v-model="form.nickname" maxlength="20" placeholder="请输入昵称" />
      <input class="input" type="number" maxlength="11" v-model="form.phone" placeholder="请输入手机号" />
      <input class="input" v-model="form.inviteCode" maxlength="32" placeholder="请输入邀请码（选填）" />
      <input class="input" password v-model="form.password" maxlength="32" placeholder="请输入登录密码" />
      <view class="qr-row">
        <view class="qr-item" @click="chooseImage('wechatPayQr')">
          <image v-if="previews.wechatPayQr" class="qr" :src="previews.wechatPayQr" mode="aspectFit" />
          <view v-else class="qr empty">+</view>
          <view class="qr-tip">{{ uploadingField === 'wechatPayQr' ? '上传中…' : '微信支付码' }}</view>
        </view>
        <view class="qr-item" @click="chooseImage('wechatReceiveQr')">
          <image v-if="previews.wechatReceiveQr" class="qr" :src="previews.wechatReceiveQr" mode="aspectFit" />
          <view v-else class="qr empty">+</view>
          <view class="qr-tip">{{ uploadingField === 'wechatReceiveQr' ? '上传中…' : '支付宝收款码' }}</view>
        </view>
      </view>
    </view>

    <view class="agree" @click="agreed = !agreed">
      <view class="box" :class="{ on: agreed }" />
      <view class="agree-text">
        我已阅读并遵守平台的
        <text class="link" @click.stop="openAgreement">《注册协议》</text>
      </view>
    </view>

    <view v-if="apiHint" class="err">{{ apiHint }}</view>

    <button class="btn" :disabled="loading || uploading" @click="submit">{{ loading ? '请稍候…' : '注册' }}</button>
    <view class="switch" @click="goLogin">已有账号，去登录</view>
  </view>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { mallH5Api, resolveMallFileUrl, saveMallToken } from '@/api/business/mall/mall-h5-api';
  import { captureMallInvite } from '@/utils/mall-invite';
  import { SmartToast } from '@/lib/smart-support';

  const loading = ref(false);
  const uploading = ref(false);
  const uploadingField = ref('');
  const agreed = ref(false);
  const apiHint = ref('');
  const avatarPreview = ref('');
  const previews = reactive({
    wechatPayQr: '',
    wechatReceiveQr: '',
  });
  const form = reactive({
    nickname: '',
    phone: '',
    password: '',
    inviteCode: captureMallInvite(),
    avatar: '',
    wechatPayQr: '',
    wechatReceiveQr: '',
  });

  function chooseAvatar() {
    chooseImage('avatar');
  }

  function chooseImage(field) {
    if (uploading.value) {
      return;
    }
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const path = res.tempFilePaths && res.tempFilePaths[0];
        if (!path) {
          return;
        }
        setPreview(field, path);
        uploading.value = true;
        uploadingField.value = field;
        try {
          const up = await mallH5Api.uploadAvatar(path);
          const url = (up.data && (up.data.fileUrl || up.data.fileKey)) || '';
          form[field] = url;
          setPreview(field, url ? resolveMallFileUrl(url) : '');
        } catch (e) {
          form[field] = '';
          setPreview(field, '');
          const msg = (e && e.msg) || (e && e.message) || '图片上传失败';
          if (msg && msg !== 'network' && msg !== 'no-h5-file') {
            apiHint.value = msg;
          }
        } finally {
          uploading.value = false;
          uploadingField.value = '';
        }
      },
    });
  }

  function setPreview(field, url) {
    if (field === 'avatar') {
      avatarPreview.value = url;
      return;
    }
    previews[field] = url;
  }

  onLoad((options) => {
    const invite = captureMallInvite(options);
    if (invite) {
      form.inviteCode = invite;
    }
  });

  function goLogin() {
    uni.navigateBack({
      fail: () => uni.redirectTo({ url: '/pages/mall/login' }),
    });
  }

  function openAgreement() {
    uni.showModal({
      title: '注册协议',
      content: '注册即表示您同意平台服务规则。请妥善保管账号与登录密码，勿向他人泄露。',
      showCancel: false,
      confirmText: '我知道了',
    });
  }

  function validate() {
    const nickname = String(form.nickname || '').trim();
    const phone = String(form.phone || '').trim();
    const password = String(form.password || '');
    if (!nickname) {
      return '请输入昵称';
    }
    if (!/^1\d{10}$/.test(phone)) {
      return '请输入11位手机号';
    }
    if (password.length < 6) {
      return '登录密码至少6位';
    }
    if (uploading.value) {
      return '图片正在上传，请稍候';
    }
    if (!form.wechatPayQr) {
      return '请上传微信支付码';
    }
    if (!form.wechatReceiveQr) {
      return '请上传支付宝收款码';
    }
    if (!agreed.value) {
      return '请先阅读并同意注册协议';
    }
    return '';
  }

  async function submit() {
    const error = validate();
    if (error) {
      SmartToast.toast(error);
      return;
    }
    loading.value = true;
    apiHint.value = '';
    try {
      const res = await mallH5Api.register({
        nickname: String(form.nickname).trim(),
        phone: String(form.phone).trim(),
        password: form.password,
        inviteCode: String(form.inviteCode || '').trim(),
        avatar: form.avatar,
        wechatPayQr: form.wechatPayQr,
        wechatReceiveQr: form.wechatReceiveQr,
      });
      saveMallToken(res.data.token);
      SmartToast.success('注册成功');
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
</script>

<style lang="scss" scoped>
  .register {
    min-height: 100vh;
    background: #fff;
    padding: 48rpx 48rpx 80rpx;
    box-sizing: border-box;
  }

  .title {
    font-size: 48rpx;
    font-weight: 700;
    color: #111;
    margin-bottom: 32rpx;
  }

  .avatar-wrap {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 24rpx;
  }

  .avatar {
    width: 144rpx;
    height: 144rpx;
    border-radius: 50%;
    background: #f5f5f5;
  }

  .avatar.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    color: #bbb;
    border: 2rpx dashed #ddd;
    box-sizing: border-box;
  }

  .avatar-tip {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #999;
  }

  .form {
    margin-top: 12rpx;
  }

  .qr-row {
    display: flex;
    justify-content: space-between;
    margin-top: 36rpx;
  }

  .qr-item {
    width: 48%;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .qr {
    width: 220rpx;
    height: 220rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
  }

  .qr.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    color: #bbb;
    border: 2rpx dashed #ddd;
    box-sizing: border-box;
  }

  .qr-tip {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #666;
  }

  .input {
    height: 96rpx;
    border-bottom: 1px solid #eee;
    font-size: 30rpx;
    color: #333;
  }

  .agree {
    display: flex;
    align-items: flex-start;
    margin-top: 40rpx;
  }

  .box {
    width: 28rpx;
    height: 28rpx;
    border: 2rpx solid #c8c8c8;
    border-radius: 4rpx;
    margin-right: 12rpx;
    margin-top: 6rpx;
    flex-shrink: 0;
    box-sizing: border-box;
  }

  .box.on {
    background: #ee0a24;
    border-color: #ee0a24;
  }

  .agree-text {
    font-size: 24rpx;
    color: #666;
    line-height: 1.6;
  }

  .link {
    color: #1677ff;
  }

  .err {
    margin-top: 24rpx;
    color: #ee0a24;
    font-size: 24rpx;
    line-height: 1.5;
  }

  .btn {
    margin-top: 56rpx;
    height: 88rpx;
    line-height: 88rpx;
    background: #ee0a24;
    color: #fff;
    border: none;
    border-radius: 44rpx;
    font-size: 32rpx;
  }

  .btn[disabled] {
    opacity: 0.6;
  }

  .switch {
    text-align: center;
    color: #1677ff;
    margin-top: 32rpx;
    font-size: 26rpx;
  }
</style>
