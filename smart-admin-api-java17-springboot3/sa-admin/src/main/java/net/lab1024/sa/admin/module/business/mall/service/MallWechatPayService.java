package net.lab1024.sa.admin.module.business.mall.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.config.MallProperties;
import net.lab1024.sa.admin.module.business.mall.constant.MallOrderStatusEnum;
import net.lab1024.sa.admin.module.business.mall.constant.MallPayStatusEnum;
import net.lab1024.sa.admin.module.business.mall.dao.MallMemberDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallOrderDao;
import net.lab1024.sa.admin.module.business.mall.dao.SeckillActivityDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallMemberEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallOrderEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallWechatPrepayForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallWechatPayVO;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayTradeTypeEnum;
import net.lab1024.sa.admin.module.business.pay.dao.PayOrderDao;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayOrderEntity;
import net.lab1024.sa.admin.module.business.pay.service.WeChatPayClient;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class MallWechatPayService {

    @Resource
    private WeChatPayClient weChatPayClient;

    @Resource
    private PayOrderDao payOrderDao;

    @Resource
    private MallOrderDao mallOrderDao;

    @Resource
    private MallMemberDao mallMemberDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private MallMemberService mallMemberService;

    @Resource
    private MallProperties mallProperties;

    @Lazy
    @Resource
    private MallOrderService mallOrderService;

    public boolean isReady() {
        return weChatPayClient.isEnabled() && weChatPayClient.isConfigured();
    }

    public void fillConfig(net.lab1024.sa.admin.module.business.mall.domain.vo.MallConfigVO vo) {
        vo.setWechatPayEnabled(isReady());
        vo.setWechatPayMock(weChatPayClient.isMock());
        vo.setWechatJsapiReady(isReady() && weChatPayClient.hasAppSecret());
    }

    public void attachAfterCreate(MallOrderEntity order) {
        if (!isReady() || order == null) {
            return;
        }
        try {
            ensurePayOrder(order, PayTradeTypeEnum.NATIVE.getValue(), null);
        } catch (Exception e) {
            log.warn("秒杀单关联微信支付失败 orderNo={}: {}", order.getOrderNo(), e.getMessage());
        }
    }

    public void closeIfAny(MallOrderEntity order) {
        if (order == null || !isReady()) {
            return;
        }
        PayOrderEntity pay = findByMall(order);
        if (pay == null || !PayStatusEnum.WAIT_PAY.equalsValue(pay.getPayStatus())) {
            return;
        }
        try {
            weChatPayClient.closeOrder(pay.getOrderNo());
        } catch (Exception e) {
            log.warn("关闭微信单失败 orderNo={}: {}", pay.getOrderNo(), e.getMessage());
        }
        pay.setPayStatus(PayStatusEnum.CLOSED.getValue());
        pay.setCloseTime(LocalDateTime.now());
        payOrderDao.updateById(pay);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<MallWechatPayVO> prepay(MallWechatPrepayForm form) {
        if (!isReady()) {
            return ResponseDTO.userErrorParam("微信支付未启用，请使用收款码转账");
        }
        MallOrderEntity order = mallOrderService.requireOwnOrderForPay(form.getOrderId());
        if (!MallOrderStatusEnum.WAIT_PAY.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("当前订单不是待付款");
        }
        String type = StringUtils.defaultIfBlank(form.getTradeType(), "native").toLowerCase();
        Integer tradeType = PayTradeTypeEnum.NATIVE.getValue();
        if ("jsapi".equals(type)) {
            tradeType = PayTradeTypeEnum.JSAPI.getValue();
        } else if ("h5".equals(type)) {
            tradeType = PayTradeTypeEnum.H5.getValue();
        }
        String openid = StringUtils.trimToNull(form.getOpenid());
        if (openid == null) {
            MallMemberEntity member = mallMemberDao.selectById(order.getMemberId());
            if (member != null) {
                openid = member.getWechatOpenid();
            }
        }
        PayOrderEntity pay = ensurePayOrder(order, tradeType, openid);
        int expire = mallProperties.getSeckill().getPayTimeoutMinutes();
        int fen = yuanToFen(order.getAmount());
        MallWechatPayVO vo = baseVo();
        vo.setTradeType(type);
        try {
            if (PayTradeTypeEnum.JSAPI.equalsValue(tradeType)) {
                if (StringUtils.isBlank(openid)) {
                    vo.setNeedOpenid(true);
                    return ResponseDTO.ok(vo);
                }
                PrepayWithRequestPaymentResponse js = weChatPayClient.prepayJsapi(
                        pay.getOrderNo(), pay.getDescription(), fen, openid, expire);
                vo.setJsapiAppId(js.getAppId());
                vo.setJsapiTimeStamp(js.getTimeStamp());
                vo.setJsapiNonceStr(js.getNonceStr());
                vo.setJsapiPackage(js.getPackageVal());
                vo.setJsapiSignType(js.getSignType());
                vo.setJsapiPaySign(js.getPaySign());
                pay.setOpenid(openid);
            } else if (PayTradeTypeEnum.H5.equalsValue(tradeType)) {
                vo.setH5Url(weChatPayClient.prepayH5(pay.getOrderNo(), pay.getDescription(), fen, form.getClientIp(), expire));
            } else {
                String codeUrl = weChatPayClient.prepayNative(pay.getOrderNo(), pay.getDescription(), fen, expire);
                pay.setCodeUrl(codeUrl);
                vo.setCodeUrl(codeUrl);
                vo.setQrcodeBase64(toQrcode(codeUrl));
            }
        } catch (BusinessException e) {
            return ResponseDTO.userErrorParam(e.getMessage());
        }
        pay.setTradeType(tradeType);
        payOrderDao.updateById(pay);
        order.setPayChannel(20);
        mallOrderDao.updateById(order);
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> mockPay(Long orderId) {
        if (!weChatPayClient.isMock()) {
            return ResponseDTO.userErrorParam("仅演示模式可模拟支付");
        }
        MallOrderEntity order = mallOrderService.requireOwnOrderForPay(orderId);
        PayOrderEntity pay = ensurePayOrder(order, PayTradeTypeEnum.NATIVE.getValue(), null);
        if (!PayStatusEnum.WAIT_PAY.equalsValue(pay.getPayStatus())) {
            return ResponseDTO.userErrorParam("支付单不是待支付");
        }
        pay.setPayStatus(PayStatusEnum.SUCCESS.getValue());
        pay.setTransactionId("MOCK" + RandomStringUtils.randomNumeric(12));
        pay.setSuccessTime(LocalDateTime.now());
        pay.setPayerTotal(pay.getAmount());
        payOrderDao.updateById(pay);
        onPaySuccess(pay.getOrderNo(), pay.getTransactionId());
        return ResponseDTO.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public void onPaySuccess(String outTradeNo, String transactionId) {
        if (StringUtils.isBlank(outTradeNo)) {
            return;
        }
        PayOrderEntity pay = payOrderDao.selectByOrderNo(outTradeNo);
        MallOrderEntity order = null;
        if (pay != null && pay.getMallOrderId() != null) {
            order = mallOrderDao.selectById(pay.getMallOrderId());
        }
        if (order == null) {
            order = mallOrderDao.selectOne(new LambdaQueryWrapper<MallOrderEntity>()
                    .eq(MallOrderEntity::getOrderNo, outTradeNo)
                    .last("LIMIT 1"));
        }
        if (order == null || Boolean.TRUE.equals(order.getDeletedFlag())) {
            return;
        }
        if (!MallOrderStatusEnum.WAIT_PAY.equalsValue(order.getOrderStatus())
                && !MallOrderStatusEnum.WAIT_CONFIRM.equalsValue(order.getOrderStatus())) {
            return;
        }
        order.setOrderStatus(MallOrderStatusEnum.WAIT_SHIP.getValue());
        order.setPayStatus(MallPayStatusEnum.PAID.getValue());
        order.setPayTime(LocalDateTime.now());
        order.setPayChannel(20);
        order.setWxTransactionId(transactionId);
        mallOrderDao.updateById(order);
        mallOrderService.freezeCommissionPublic(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> refundClose(Long orderId, String reason) {
        if (!isReady()) {
            return ResponseDTO.userErrorParam("微信支付未启用");
        }
        MallOrderEntity order = mallOrderDao.selectById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("订单不存在");
        }
        if (!Integer.valueOf(20).equals(order.getPayChannel())) {
            return ResponseDTO.userErrorParam("不是微信支付订单");
        }
        if (MallOrderStatusEnum.CLOSED.equalsValue(order.getOrderStatus())
                || MallOrderStatusEnum.COMPLETED.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("当前状态不能退款关单");
        }
        PayOrderEntity pay = findByMall(order);
        if (pay == null) {
            return ResponseDTO.userErrorParam("找不到对应支付单");
        }
        String remark = StringUtils.defaultIfBlank(StringUtils.trimToNull(reason), "商家微信退款关单");
        if (PayStatusEnum.SUCCESS.equalsValue(pay.getPayStatus()) || PayStatusEnum.REFUNDING.equalsValue(pay.getPayStatus())) {
            String refundNo = "RF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + RandomStringUtils.randomNumeric(4);
            if (weChatPayClient.isMock()) {
                pay.setRefundId("MOCKRF" + RandomStringUtils.randomNumeric(10));
            } else {
                weChatPayClient.refund(pay.getOrderNo(), refundNo, pay.getAmount(), pay.getAmount(), remark);
            }
            pay.setRefundNo(refundNo);
            pay.setRefundAmount(pay.getAmount());
            pay.setRefundTime(LocalDateTime.now());
            pay.setPayStatus(PayStatusEnum.REFUND.getValue());
            payOrderDao.updateById(pay);
        }
        order.setOrderStatus(MallOrderStatusEnum.CLOSED.getValue());
        order.setPayStatus(MallPayStatusEnum.CLOSED.getValue());
        order.setCloseTime(LocalDateTime.now());
        order.setRemark(remark);
        mallOrderDao.updateById(order);
        seckillActivityDao.restoreStock(order.getActivityId(), order.getQty());
        mallOrderService.cancelFrozenCommission(order.getOrderId());
        return ResponseDTO.ok();
    }

    public ResponseDTO<String> oauthUrl(String redirectUri, String state) {
        if (!weChatPayClient.hasAppSecret()) {
            return ResponseDTO.userErrorParam("未配置 app-secret，无法网页授权");
        }
        if (StringUtils.isBlank(redirectUri)) {
            return ResponseDTO.userErrorParam("缺少回调地址");
        }
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                + weChatPayClient.getProperties().getAppId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code&scope=snsapi_base&state="
                + URLEncoder.encode(StringUtils.defaultString(state), StandardCharsets.UTF_8)
                + "#wechat_redirect";
        return ResponseDTO.ok(url);
    }

    public ResponseDTO<String> oauthCallback(String code) {
        if (StringUtils.isBlank(code)) {
            return ResponseDTO.userErrorParam("缺少 code");
        }
        if (!weChatPayClient.hasAppSecret()) {
            return ResponseDTO.userErrorParam("未配置 app-secret");
        }
        MallMemberEntity member = mallMemberService.requireMember();
        String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                + weChatPayClient.getProperties().getAppId()
                + "&secret=" + weChatPayClient.getProperties().getAppSecret()
                + "&code=" + code + "&grant_type=authorization_code";
        String body = new RestTemplate().getForObject(tokenUrl, String.class);
        JSONObject json = JSON.parseObject(body);
        String openid = json == null ? null : json.getString("openid");
        if (StringUtils.isBlank(openid)) {
            return ResponseDTO.userErrorParam("获取 openid 失败：" + StringUtils.defaultString(body));
        }
        member.setWechatOpenid(openid);
        mallMemberDao.updateById(member);
        return ResponseDTO.ok(openid);
    }

    private PayOrderEntity ensurePayOrder(MallOrderEntity order, Integer tradeType, String openid) {
        PayOrderEntity exist = findByMall(order);
        if (exist != null) {
            if (StringUtils.isNotBlank(openid)) {
                exist.setOpenid(openid);
            }
            return exist;
        }
        PayOrderEntity entity = new PayOrderEntity();
        entity.setOrderNo(order.getOrderNo());
        entity.setMallOrderId(order.getOrderId());
        entity.setPayChannel(PayChannelEnum.WECHAT.getValue());
        entity.setDescription(StringUtils.abbreviate(order.getGoodsName(), 120));
        entity.setAmount(yuanToFen(order.getAmount()));
        entity.setTradeType(tradeType);
        entity.setPayStatus(PayStatusEnum.WAIT_PAY.getValue());
        entity.setOpenid(openid);
        entity.setRefundAmount(0);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        payOrderDao.insert(entity);
        return entity;
    }

    private PayOrderEntity findByMall(MallOrderEntity order) {
        PayOrderEntity byMall = payOrderDao.selectOne(new LambdaQueryWrapper<PayOrderEntity>()
                .eq(PayOrderEntity::getMallOrderId, order.getOrderId())
                .eq(PayOrderEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        if (byMall != null) {
            return byMall;
        }
        return payOrderDao.selectByOrderNo(order.getOrderNo());
    }

    private MallWechatPayVO baseVo() {
        MallWechatPayVO vo = new MallWechatPayVO();
        vo.setWechatReady(isReady());
        vo.setMock(weChatPayClient.isMock());
        vo.setNeedOpenid(false);
        return vo;
    }

    private String toQrcode(String content) {
        return "data:image/png;base64," + Base64.encode(QrCodeUtil.generatePng(content, 280, 280));
    }

    private int yuanToFen(BigDecimal yuan) {
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
