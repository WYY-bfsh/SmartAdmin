package net.lab1024.sa.admin.module.business.pay.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson.JSON;
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
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayTradeTypeEnum;
import net.lab1024.sa.admin.module.business.pay.dao.PayOrderDao;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayOrderEntity;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderCreateForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayOrderQueryForm;
import net.lab1024.sa.admin.module.business.pay.domain.form.PayRefundForm;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 微信支付订单
 */
@Slf4j
@Service
public class PayOrderService {

    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Resource
    private PayOrderDao payOrderDao;

    @Resource
    private WeChatPayClient weChatPayClient;

    public ResponseDTO<WeChatPayConfigVO> getConfig() {
        WeChatPayConfigVO vo = new WeChatPayConfigVO();
        vo.setEnabled(weChatPayClient.isEnabled());
        vo.setConfigured(weChatPayClient.isConfigured());
        vo.setAppId(mask(weChatPayClient.getProperties().getAppId()));
        vo.setMchId(weChatPayClient.getProperties().getMchId());
        vo.setNotifyUrl(weChatPayClient.getProperties().getNotifyUrl());
        vo.setPrivateKeyReady(StringUtils.isNotBlank(weChatPayClient.getProperties().getPrivateKey())
                || StringUtils.isNotBlank(weChatPayClient.getProperties().getPrivateKeyPath()));
        return ResponseDTO.ok(vo);
    }

    public ResponseDTO<PageResult<PayOrderVO>> query(PayOrderQueryForm queryForm) {
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
        PayOrderVO vo = SmartBeanUtil.copy(entity, PayOrderVO.class);
        fillAmountYuan(vo);
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<PayCreateVO> create(PayOrderCreateForm createForm) {
        int amountFen = yuanToFen(createForm.getAmountYuan());
        String orderNo = generateOrderNo();

        PayOrderEntity entity = new PayOrderEntity();
        entity.setOrderNo(orderNo);
        entity.setDescription(createForm.getDescription());
        entity.setAmount(amountFen);
        entity.setTradeType(PayTradeTypeEnum.NATIVE.getValue());
        entity.setPayStatus(PayStatusEnum.WAIT_PAY.getValue());
        entity.setRefundAmount(0);
        entity.setRemark(createForm.getRemark());
        entity.setCreateUserId(AdminRequestUtil.getRequestUserId());
        entity.setDeletedFlag(Boolean.FALSE);
        payOrderDao.insert(entity);

        String codeUrl;
        try {
            codeUrl = weChatPayClient.prepayNative(orderNo, createForm.getDescription(), amountFen);
        } catch (ServiceException e) {
            log.error("微信下单失败, orderNo={}, code={}, msg={}", orderNo, e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException("微信下单失败：" + e.getErrorMessage());
        }

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
        return ResponseDTO.ok(vo);
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
        return ResponseDTO.ok(vo);
    }

    public ResponseDTO<PayOrderVO> sync(Long payOrderId) {
        PayOrderEntity entity = requireOrder(payOrderId);
        try {
            Transaction transaction = weChatPayClient.queryByOutTradeNo(entity.getOrderNo());
            applyTransaction(entity, transaction);
            payOrderDao.updateById(entity);
        } catch (ServiceException e) {
            log.error("同步微信支付状态失败, orderNo={}, code={}, msg={}", entity.getOrderNo(), e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException("同步微信订单失败：" + e.getErrorMessage());
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
            weChatPayClient.closeOrder(entity.getOrderNo());
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
        Refund refund;
        try {
            refund = weChatPayClient.refund(entity.getOrderNo(), refundNo, entity.getAmount(), refundFen, refundForm.getReason());
        } catch (ServiceException e) {
            log.error("微信退款失败, orderNo={}, code={}, msg={}", entity.getOrderNo(), e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException("微信退款失败：" + e.getErrorMessage());
        }

        entity.setRefundNo(refundNo);
        entity.setRefundId(refund.getRefundId());
        entity.setRefundAmount(alreadyRefund + refundFen);
        entity.setRefundTime(LocalDateTime.now());
        if (entity.getRefundAmount() >= entity.getAmount()) {
            entity.setPayStatus(refund.getStatus() == Status.SUCCESS ? PayStatusEnum.REFUND.getValue() : PayStatusEnum.REFUNDING.getValue());
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
        return successJson();
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

    private String generateOrderNo() {
        return "WX" + LocalDateTime.now().format(ORDER_NO_TIME) + RandomStringUtils.randomNumeric(4);
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
}
