package net.lab1024.sa.admin.module.business.pay.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayTradeTypeEnum;
import net.lab1024.sa.admin.module.business.pay.dao.PayOrderDao;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayOrderEntity;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderCreateForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayRefundForm;
import net.lab1024.sa.admin.module.business.pay.domain.vo.AlipayConfigVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayCreateVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.PayOrderVO;
import net.lab1024.sa.admin.module.business.pay.domain.vo.WeChatPayConfigVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import net.lab1024.sa.admin.module.business.mall.service.MallWechatPayService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 微信支付订单
 */
@Slf4j
@Service
public class PayOrderService {

    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private volatile boolean demoSeedAttempted = false;

    @Resource
    private PayOrderDao payOrderDao;

    @Resource
    private WeChatPayClient weChatPayClient;

    @Resource
    private AlipayClient alipayClient;

    @Lazy
    @Resource
    private MallWechatPayService mallWechatPayService;

    public ResponseDTO<WeChatPayConfigVO> getConfig() {
        WeChatPayConfigVO vo = new WeChatPayConfigVO();
        vo.setEnabled(weChatPayClient.isEnabled());
        vo.setMock(weChatPayClient.isMock());
        vo.setConfigured(weChatPayClient.isConfigured());
        String appId = weChatPayClient.getProperties().getAppId();
        vo.setAppId(weChatPayClient.isMock() ? appId : mask(appId));
        vo.setMchId(weChatPayClient.getProperties().getMchId());
        vo.setNotifyUrl(weChatPayClient.getProperties().getNotifyUrl());
        vo.setPrivateKeyReady(StringUtils.isNotBlank(weChatPayClient.getProperties().getPrivateKey())
                || StringUtils.isNotBlank(weChatPayClient.getProperties().getPrivateKeyPath()));
        vo.setAppSecretReady(weChatPayClient.hasAppSecret());
        vo.setH5AppUrl(weChatPayClient.getProperties().getH5AppUrl());
        return ResponseDTO.ok(vo);
    }

    public ResponseDTO<PageResult<PayOrderVO>> query(PayOrderQueryForm queryForm) {
        seedDemoOrdersIfNeeded();
        queryForm.setDeletedFlag(false);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PayOrderVO> list = payOrderDao.query(page, queryForm);
        list.forEach(this::fillAmountYuan);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<PayOrderVO> detail(Long payOrderId) {
        PayOrderEntity entity = payOrderDao.selectById(payOrderId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("支付订单不存在");
        }
        if (isChannelMock(entity)) {
            applyMockAutoPay(entity);
        }
        PayOrderVO vo = SmartBeanUtil.copy(entity, PayOrderVO.class);
        fillAmountYuan(vo);
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<PayCreateVO> create(PayOrderCreateForm createForm) {
        int amountFen = yuanToFen(createForm.getAmountYuan());
        int channel = createForm.getPayChannel() == null
                ? PayChannelEnum.WECHAT.getValue() : createForm.getPayChannel();
        String orderNo = generateOrderNo(channel);

        PayOrderEntity entity = new PayOrderEntity();
        entity.setOrderNo(orderNo);
        entity.setPayChannel(channel);
        entity.setDescription(createForm.getDescription());
        entity.setAmount(amountFen);
        entity.setTradeType(PayTradeTypeEnum.NATIVE.getValue());
        entity.setPayStatus(PayStatusEnum.WAIT_PAY.getValue());
        entity.setRefundAmount(0);
        entity.setRemark(createForm.getRemark());
        entity.setCreateUserId(AdminRequestUtil.getRequestUserId());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        payOrderDao.insert(entity);

        String codeUrl = PayChannelEnum.ALIPAY.getValue().equals(channel)
                ? alipayClient.precreate(orderNo, createForm.getDescription(), fenToYuan(amountFen).toPlainString(), null)
                : weChatCreateCodeUrl(orderNo, createForm.getDescription(), amountFen);

        entity.setCodeUrl(codeUrl);
        payOrderDao.updateById(entity);

        PayCreateVO vo = new PayCreateVO();
        vo.setPayOrderId(entity.getPayOrderId());
        vo.setOrderNo(orderNo);
        vo.setDescription(entity.getDescription());
        vo.setAmountYuan(fenToYuan(amountFen));
        vo.setCodeUrl(codeUrl);
        vo.setQrcodeBase64(toQrcodeBase64(codeUrl));
        vo.setPayStatus(entity.getPayStatus());
        vo.setMock(PayChannelEnum.ALIPAY.getValue().equals(channel) ? alipayClient.isMock() : weChatPayClient.isMock());
        return ResponseDTO.ok(vo);
    }

    private String weChatCreateCodeUrl(String orderNo, String description, int amountFen) {
        try {
            return weChatPayClient.prepayNative(orderNo, description, amountFen);
        } catch (ServiceException e) {
            log.error("微信下单失败, orderNo={}, code={}, msg={}", orderNo, e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException("微信下单失败：" + e.getErrorMessage());
        }
    }

    public ResponseDTO<PayCreateVO> qrcode(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        if (!PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
            return ResponseDTO.userErrorParam("当前订单不是待支付状态");
        }
        if (StringUtils.isBlank(entity.getCodeUrl())) {
            return ResponseDTO.userErrorParam("支付二维码不存在，请重新下单");
        }
        PayCreateVO vo = new PayCreateVO();
        vo.setPayOrderId(entity.getPayOrderId());
        vo.setOrderNo(entity.getOrderNo());
        vo.setDescription(entity.getDescription());
        vo.setAmountYuan(fenToYuan(entity.getAmount()));
        vo.setCodeUrl(entity.getCodeUrl());
        vo.setQrcodeBase64(toQrcodeBase64(entity.getCodeUrl()));
        vo.setPayStatus(entity.getPayStatus());
        vo.setMock(isChannelMock(entity));
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<PayOrderVO> mockPay(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        if (!isChannelMock(entity)) {
            return ResponseDTO.userErrorParam(PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity))
                    ? "仅演示模式支持模拟支付，正式商户请用支付宝扫码"
                    : "仅演示模式支持模拟支付，正式商户请用微信扫码");
        }
        if (!PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
            return ResponseDTO.userErrorParam("当前订单不是待支付状态");
        }
        mockPaySuccess(entity);
        payOrderDao.updateById(entity);
        notifyMallPaid(entity);
        PayOrderVO vo = SmartBeanUtil.copy(entity, PayOrderVO.class);
        fillAmountYuan(vo);
        return ResponseDTO.ok(vo);
    }

    public ResponseDTO<PayOrderVO> sync(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        if (isChannelMock(entity)) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
                mockPaySuccess(entity);
                payOrderDao.updateById(entity);
                notifyMallPaid(entity);
            }
        } else if (PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity))) {
            try {
                com.alipay.api.response.AlipayTradeQueryResponse res = alipayClient.query(entity.getOrderNo());
                applyAlipayTradeState(entity, res);
                payOrderDao.updateById(entity);
                notifyMallPaid(entity);
            } catch (Exception e) {
                log.error("同步支付宝订单状态失败, orderNo={}", entity.getOrderNo(), e);
                throw new BusinessException("同步支付宝订单失败：" + e.getMessage());
            }
        } else {
            try {
                Transaction transaction = weChatPayClient.queryByOutTradeNo(entity.getOrderNo());
                applyTransaction(entity, transaction);
                payOrderDao.updateById(entity);
                notifyMallPaid(entity);
            } catch (ServiceException e) {
                log.error("同步微信支付状态失败, orderNo={}, code={}, msg={}", entity.getOrderNo(), e.getErrorCode(), e.getErrorMessage());
                throw new BusinessException("同步微信订单失败：" + e.getErrorMessage());
            }
        }
        PayOrderVO vo = SmartBeanUtil.copy(entity, PayOrderVO.class);
        fillAmountYuan(vo);
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> close(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        if (!PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
            return ResponseDTO.userErrorParam("只有待支付订单可以关闭");
        }
        try {
            if (PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity))) {
                alipayClient.close(entity.getOrderNo());
            } else {
                weChatPayClient.closeOrder(entity.getOrderNo());
            }
        } catch (ServiceException e) {
            log.error("关闭微信支付订单失败, orderNo={}, code={}, msg={}", entity.getOrderNo(), e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException("关闭微信订单失败：" + e.getErrorMessage());
        }
        entity.setPayStatus(PayStatusEnum.CLOSED.getValue());
        entity.setCloseTime(LocalDateTime.now());
        payOrderDao.updateById(entity);
        return ResponseDTO.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> refund(PayRefundForm refundForm) {
        PayOrderEntity entity = requireOrder(refundForm.getPayOrderId());
        if (!PayStatusEnum.SUCCESS.equalsValue(entity.getPayStatus()) && !PayStatusEnum.REFUNDING.equalsValue(entity.getPayStatus())) {
            return ResponseDTO.userErrorParam("只有支付成功的订单可以退款");
        }
        int refundFen = yuanToFen(refundForm.getRefundAmountYuan());
        int alreadyRefund = entity.getRefundAmount() == null ? 0 : entity.getRefundAmount();
        if (alreadyRefund + refundFen > entity.getAmount()) {
            return ResponseDTO.userErrorParam("退款金额不能超过剩余可退金额");
        }

        String refundNo = generateRefundNo();
        String refundId;
        boolean refundSuccess = true;
        boolean isAlipay = PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity));

        if (isAlipay ? alipayClient.isMock() : weChatPayClient.isMock()) {
            refundId = "5000000" + RandomStringUtils.randomNumeric(18);
        } else if (isAlipay) {
            com.alipay.api.response.AlipayTradeRefundResponse res = alipayClient.refund(
                    entity.getOrderNo(), refundNo, fenToYuan(refundFen).toPlainString(), refundForm.getReason());
            refundId = res == null ? null : res.getTradeNo();
            refundSuccess = true;
        } else {
            Refund refund;
            try {
                refund = weChatPayClient.refund(entity.getOrderNo(), refundNo, entity.getAmount(), refundFen, refundForm.getReason());
            } catch (ServiceException e) {
                log.error("微信退款失败, orderNo={}, code={}, msg={}", entity.getOrderNo(), e.getErrorCode(), e.getErrorMessage());
                throw new BusinessException("微信退款失败：" + e.getErrorMessage());
            }
            refundId = refund.getRefundId();
            refundSuccess = refund.getStatus() == Status.SUCCESS;
        }

        entity.setRefundNo(refundNo);
        entity.setRefundId(refundId);
        entity.setRefundAmount(alreadyRefund + refundFen);
        entity.setRefundTime(LocalDateTime.now());
        if (entity.getRefundAmount() >= entity.getAmount()) {
            entity.setPayStatus(refundSuccess ? PayStatusEnum.REFUND.getValue() : PayStatusEnum.REFUNDING.getValue());
        } else if (isAlipay ? alipayClient.isMock() : weChatPayClient.isMock()) {
            entity.setPayStatus(PayStatusEnum.REFUNDING.getValue());
        }
        if (StringUtils.isNotBlank(refundForm.getReason())) {
            entity.setRemark(StringUtils.defaultString(entity.getRemark()) + " 退款：" + refundForm.getReason());
        }
        payOrderDao.updateById(entity);
        return ResponseDTO.ok();
    }

    public String handleNotify(HttpServletRequest request) {
        String body;
        try {
            body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取微信支付回调失败", e);
            return failJson("读取回调失败");
        }

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signType(request.getHeader("Wechatpay-Signature-Type"))
                .body(body)
                .build();

        Transaction transaction;
        try {
            transaction = weChatPayClient.parsePayNotify(requestParam);
        } catch (ValidationException e) {
            log.error("微信支付回调验签失败", e);
            return failJson("验签失败");
        } catch (Exception e) {
            log.error("解析微信支付回调失败", e);
            return failJson("解析失败");
        }

        PayOrderEntity entity = payOrderDao.selectByOrderNo(transaction.getOutTradeNo());
        if (entity == null) {
            log.error("微信支付回调找不到订单, outTradeNo={}", transaction.getOutTradeNo());
            return failJson("订单不存在");
        }
        entity.setNotifyContent(JSON.toJSONString(transaction));
        applyTransaction(entity, transaction);
        payOrderDao.updateById(entity);
        notifyMallPaid(entity);
        return successJson();
    }

    /**
     * 支付成功后回写秒杀订单。
     * 微信与支付宝共用这一个入口：mall 侧的回写逻辑与渠道无关，
     * 为不改动 mall 包（可能被并行修改），这里不新增方法。
     */
    private void notifyMallPaid(PayOrderEntity entity) {
        if (entity == null || !PayStatusEnum.SUCCESS.equalsValue(entity.getPayStatus())) {
            return;
        }
        try {
            mallWechatPayService.onPaySuccess(entity.getOrderNo(), entity.getTransactionId());
        } catch (Exception e) {
            log.error("支付成功回写秒杀订单失败 orderNo={}", entity.getOrderNo(), e);
        }
    }

    private void applyTransaction(PayOrderEntity entity, Transaction transaction) {
        if (transaction == null) {
            return;
        }
        if (StringUtils.isNotBlank(transaction.getTransactionId())) {
            entity.setTransactionId(transaction.getTransactionId());
        }
        if (transaction.getPayer() != null) {
            entity.setOpenid(transaction.getPayer().getOpenid());
        }
        if (transaction.getAmount() != null && transaction.getAmount().getPayerTotal() != null) {
            entity.setPayerTotal(transaction.getAmount().getPayerTotal());
        }
        Transaction.TradeStateEnum tradeState = transaction.getTradeState();
        if (tradeState == Transaction.TradeStateEnum.SUCCESS) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus()) || PayStatusEnum.CLOSED.equalsValue(entity.getPayStatus())) {
                entity.setPayStatus(PayStatusEnum.SUCCESS.getValue());
                entity.setSuccessTime(parseWechatTime(transaction.getSuccessTime()));
            }
        } else if (tradeState == Transaction.TradeStateEnum.CLOSED || tradeState == Transaction.TradeStateEnum.PAYERROR) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
                entity.setPayStatus(PayStatusEnum.CLOSED.getValue());
                entity.setCloseTime(LocalDateTime.now());
            }
        } else if (tradeState == Transaction.TradeStateEnum.REFUND) {
            entity.setPayStatus(PayStatusEnum.REFUND.getValue());
            if (entity.getRefundTime() == null) {
                entity.setRefundTime(LocalDateTime.now());
            }
        }
    }

    private PayOrderEntity requireOrder(Long payOrderId) {
        PayOrderEntity entity = payOrderDao.selectById(payOrderId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            throw new BusinessException("支付订单不存在");
        }
        return entity;
    }

    private void fillAmountYuan(PayOrderVO vo) {
        if (vo.getAmount() != null) {
            vo.setAmountYuan(fenToYuan(vo.getAmount()));
        }
        if (vo.getRefundAmount() != null) {
            vo.setRefundAmountYuan(fenToYuan(vo.getRefundAmount()));
        }
    }

    private String generateOrderNo(int channel) {
        String prefix = PayChannelEnum.ALIPAY.getValue().equals(channel) ? "ALI" : "WX";
        return prefix + LocalDateTime.now().format(ORDER_NO_TIME) + RandomStringUtils.randomNumeric(4);
    }

    private int channelOf(PayOrderEntity entity) {
        return entity.getPayChannel() == null ? PayChannelEnum.WECHAT.getValue() : entity.getPayChannel();
    }

    private boolean isChannelMock(PayOrderEntity entity) {
        return PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity))
                ? alipayClient.isMock() : weChatPayClient.isMock();
    }

    /**
     * 处理支付宝异步通知。返回给支付宝的内容由 Controller 决定（成功必须是纯文本 success）。
     */
    public boolean handleAlipayNotify(Map<String, String> params) {
        if (!alipayClient.verifyNotify(params)) {
            log.error("支付宝回调验签失败, params={}", params);
            return false;
        }
        String outTradeNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        if (StringUtils.isBlank(outTradeNo)) {
            log.error("支付宝回调缺少 out_trade_no");
            return false;
        }
        PayOrderEntity entity = payOrderDao.selectByOrderNo(outTradeNo);
        if (entity == null) {
            log.error("支付宝回调找不到订单, outTradeNo={}", outTradeNo);
            return false;
        }
        if (StringUtils.isNotBlank(totalAmount)) {
            try {
                int notifyFen = yuanToFen(new BigDecimal(totalAmount));
                if (notifyFen != (entity.getAmount() == null ? -1 : entity.getAmount())) {
                    log.error("支付宝回调金额不符, outTradeNo={}, 通知={}分, 本地={}分",
                            outTradeNo, notifyFen, entity.getAmount());
                    return false;
                }
            } catch (Exception e) {
                log.error("支付宝回调金额解析失败, totalAmount={}", totalAmount, e);
                return false;
            }
        }
        entity.setNotifyContent(JSON.toJSONString(params));
        applyAlipayNotify(entity, params);
        payOrderDao.updateById(entity);
        notifyMallPaid(entity);
        return true;
    }

    /**
     * 按支付宝通知更新本地订单状态。已退款/退款中的订单不做回退。
     */
    private void applyAlipayNotify(PayOrderEntity entity, Map<String, String> params) {
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");
        if (StringUtils.isNotBlank(tradeNo)) {
            entity.setTransactionId(tradeNo);
        }
        if (StringUtils.isNotBlank(params.get("buyer_id"))) {
            entity.setOpenid(params.get("buyer_id"));
        }
        if (StringUtils.isNotBlank(params.get("receipt_amount"))) {
            try {
                entity.setPayerTotal(yuanToFen(new BigDecimal(params.get("receipt_amount"))));
            } catch (Exception ignored) {
                // 收不到就留空，不阻断业务
            }
        }
        if (PayStatusEnum.REFUND.equalsValue(entity.getPayStatus())
                || PayStatusEnum.REFUNDING.equalsValue(entity.getPayStatus())) {
            return;
        }
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
                entity.setPayStatus(PayStatusEnum.SUCCESS.getValue());
                entity.setSuccessTime(parseAlipayTime(params.get("gmt_payment")));
            }
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
                entity.setPayStatus(PayStatusEnum.CLOSED.getValue());
                entity.setCloseTime(LocalDateTime.now());
            }
        }
    }

    /**
     * 查单结果映射到本地订单
     */
    private void applyAlipayTradeState(PayOrderEntity entity,
                                      com.alipay.api.response.AlipayTradeQueryResponse res) {
        if (res == null || !res.isSuccess()) {
            return;
        }
        if (StringUtils.isNotBlank(res.getTradeNo())) {
            entity.setTransactionId(res.getTradeNo());
        }
        if (res.getTotalAmount() != null) {
            entity.setPayerTotal(yuanToFen(new BigDecimal(res.getTotalAmount())));
        }
        String tradeStatus = res.getTradeStatus();
        if (PayStatusEnum.REFUND.equalsValue(entity.getPayStatus())
                || PayStatusEnum.REFUNDING.equalsValue(entity.getPayStatus())) {
            return;
        }
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())
                    || PayStatusEnum.CLOSED.equalsValue(entity.getPayStatus())) {
                entity.setPayStatus(PayStatusEnum.SUCCESS.getValue());
                entity.setSuccessTime(parseAlipayTime(res.getSendPayDate() == null
                        ? null : res.getSendPayDate().toString()));
            }
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            if (PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
                entity.setPayStatus(PayStatusEnum.CLOSED.getValue());
                entity.setCloseTime(LocalDateTime.now());
            }
        }
    }

    private LocalDateTime parseAlipayTime(String time) {
        if (StringUtils.isBlank(time)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(time.replace(' ', 'T'));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * 支付宝配置概览
     */
    public ResponseDTO<AlipayConfigVO> getAlipayConfig() {
        AlipayConfigVO vo = new AlipayConfigVO();
        vo.setEnabled(alipayClient.isEnabled());
        vo.setMock(alipayClient.isMock());
        vo.setSandbox(alipayClient.getProperties().getSandbox());
        vo.setSignMode(alipayClient.getProperties().getSignMode());
        vo.setConfigured(alipayClient.isConfigured());
        vo.setAppId(alipayClient.isMock()
                ? alipayClient.getProperties().getAppId()
                : mask(alipayClient.getProperties().getAppId()));
        vo.setNotifyUrl(alipayClient.getProperties().getNotifyUrl());
        vo.setPrivateKeyReady(alipayClient.hasPrivateKey());
        vo.setGatewayUrl(alipayClient.getProperties().resolveGatewayUrl());
        return ResponseDTO.ok(vo);
    }

    /**
     * 生成手机网站支付表单。注意：这个接口不登录也能访问，
     * 因此只能按 payOrderId 取订单，不能返回任何敏感信息。
     */
    public String alipayWapForm(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        if (!PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
            throw new BusinessException("当前订单不是待支付状态");
        }
        if (!PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity))) {
            throw new BusinessException("该订单不是支付宝订单");
        }
        return alipayClient.wapForm(entity.getOrderNo(), entity.getDescription(),
                fenToYuan(entity.getAmount()).toPlainString(), null);
    }

    public String alipayPageForm(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        if (!PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus())) {
            throw new BusinessException("当前订单不是待支付状态");
        }
        if (!PayChannelEnum.ALIPAY.getValue().equals(channelOf(entity))) {
            throw new BusinessException("该订单不是支付宝订单");
        }
        return alipayClient.pageForm(entity.getOrderNo(), entity.getDescription(),
                fenToYuan(entity.getAmount()).toPlainString());
    }

    private String generateRefundNo() {
        return "RF" + LocalDateTime.now().format(ORDER_NO_TIME) + RandomStringUtils.randomNumeric(4);
    }

    private String toQrcodeBase64(String content) {
        byte[] png = QrCodeUtil.generatePng(content, 280, 280);
        return "data:image/png;base64," + Base64.encode(png);
    }

    private int yuanToFen(BigDecimal yuan) {
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal fenToYuan(Integer fen) {
        return new BigDecimal(fen).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private String mask(String value) {
        if (StringUtils.isBlank(value) || value.length() <= 8) {
            return value;
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    private LocalDateTime parseWechatTime(String time) {
        if (StringUtils.isBlank(time)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(time).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String successJson() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    private String failJson(String message) {
        return "{\"code\":\"FAIL\",\"message\":\"" + message + "\"}";
    }

    private void seedDemoOrdersIfNeeded() {
        if (!weChatPayClient.isMock() || demoSeedAttempted) {
            return;
        }
        synchronized (this) {
            if (demoSeedAttempted) {
                return;
            }
            try {
                Long count = payOrderDao.selectCount(new LambdaQueryWrapper<PayOrderEntity>()
                        .eq(PayOrderEntity::getDeletedFlag, false));
                if (count != null && count > 0) {
                    demoSeedAttempted = true;
                    return;
                }
                LocalDateTime now = LocalDateTime.now();
                insertDemoOrder("WX202608221000010001", "演示-会员月卡", 1, PayStatusEnum.WAIT_PAY.getValue(),
                        "weixin://wxpay/bizpayurl?pr=DEMOWX202608221000010001", null, null, 0, null, now.minusHours(2), null);
                insertDemoOrder("WX202608211430220002", "演示-办公用品采购", 12800, PayStatusEnum.SUCCESS.getValue(),
                        null, "4200002208261234567890123456", 12800, 0, now.minusDays(2).plusHours(1), now.minusDays(2), null);
                insertDemoOrder("WX202608201015330003", "演示-已关闭订单", 990, PayStatusEnum.CLOSED.getValue(),
                        "weixin://wxpay/bizpayurl?pr=DEMOWX202608201015330003", null, null, 0, null, now.minusDays(3), now.minusDays(3).plusMinutes(20));
                insertDemoOrder("WX202608191600440004", "演示-全额退款", 6600, PayStatusEnum.REFUND.getValue(),
                        null, "4200001908261234567890123456", 6600, 6600, now.minusDays(4).plusMinutes(3), now.minusDays(4), null);
                insertDemoOrder("WX202608181100550005", "演示-部分退款", 19900, PayStatusEnum.REFUNDING.getValue(),
                        null, "4200001808261234567890123456", 19900, 5000, now.minusDays(5).plusMinutes(6), now.minusDays(5), null);
                demoSeedAttempted = true;
            } catch (Exception e) {
                log.warn("演示订单写入失败，请先执行 wechat-pay.sql 创建 t_pay_order：{}", e.getMessage());
            }
        }
    }

    private void insertDemoOrder(String orderNo, String description, int amount, int payStatus, String codeUrl,
                                 String transactionId, Integer payerTotal, int refundAmount, LocalDateTime successTime,
                                 LocalDateTime createTime, LocalDateTime closeTime) {
        PayOrderEntity entity = new PayOrderEntity();
        entity.setOrderNo(orderNo);
        entity.setPayChannel(PayChannelEnum.WECHAT.getValue());
        entity.setDescription(description);
        entity.setAmount(amount);
        entity.setTradeType(PayTradeTypeEnum.NATIVE.getValue());
        entity.setPayStatus(payStatus);
        entity.setCodeUrl(codeUrl);
        entity.setTransactionId(transactionId);
        entity.setOpenid(transactionId == null ? null : "oDEMO" + orderNo.substring(orderNo.length() - 8));
        entity.setPayerTotal(payerTotal);
        entity.setSuccessTime(successTime);
        entity.setRefundAmount(refundAmount);
        if (refundAmount > 0) {
            entity.setRefundNo("RF" + orderNo.substring(2));
            entity.setRefundId("5000000" + orderNo.substring(orderNo.length() - 12));
            entity.setRefundTime(successTime == null ? createTime.plusHours(2) : successTime.plusHours(2));
        }
        entity.setCloseTime(closeTime);
        entity.setRemark("系统演示数据");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(createTime);
        entity.setUpdateTime(createTime);
        payOrderDao.insert(entity);
    }

    private void applyMockAutoPay(PayOrderEntity entity) {
        if (!PayStatusEnum.WAIT_PAY.equalsValue(entity.getPayStatus()) || entity.getCreateTime() == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreateTime().isBefore(now.minusMinutes(10))) {
            return;
        }
        if (entity.getCreateTime().plusSeconds(8).isAfter(now)) {
            return;
        }
        mockPaySuccess(entity);
        payOrderDao.updateById(entity);
    }

    private void mockPaySuccess(PayOrderEntity entity) {
        entity.setPayStatus(PayStatusEnum.SUCCESS.getValue());
        entity.setTransactionId("420000" + LocalDateTime.now().format(ORDER_NO_TIME) + RandomStringUtils.randomNumeric(5));
        entity.setOpenid("oDEMO" + RandomStringUtils.randomAlphanumeric(22));
        entity.setPayerTotal(entity.getAmount());
        entity.setSuccessTime(LocalDateTime.now());
    }
}
