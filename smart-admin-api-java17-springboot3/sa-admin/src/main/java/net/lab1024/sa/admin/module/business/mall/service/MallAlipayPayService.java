package net.lab1024.sa.admin.module.business.mall.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.constant.MallOrderStatusEnum;
import net.lab1024.sa.admin.module.business.mall.constant.MallPayStatusEnum;
import net.lab1024.sa.admin.module.business.mall.dao.MallOrderDao;
import net.lab1024.sa.admin.module.business.mall.dao.SeckillActivityDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallOrderEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallWechatPrepayForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallAlipayPayVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallConfigVO;
import net.lab1024.sa.admin.module.business.pay.constant.PayChannelEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayStatusEnum;
import net.lab1024.sa.admin.module.business.pay.constant.PayTradeTypeEnum;
import net.lab1024.sa.admin.module.business.pay.dao.PayOrderDao;
import net.lab1024.sa.admin.module.business.pay.domain.entity.PayOrderEntity;
import net.lab1024.sa.admin.module.business.pay.service.AlipayClient;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class MallAlipayPayService {

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private PayOrderDao payOrderDao;

    @Resource
    private MallOrderDao mallOrderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Lazy
    @Resource
    private MallOrderService mallOrderService;

    public boolean isReady() {
        return alipayClient.isEnabled() && alipayClient.isConfigured();
    }

    public void fillConfig(MallConfigVO vo) {
        vo.setAlipayPayEnabled(isReady());
        vo.setAlipayPayMock(alipayClient.isMock());
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
            alipayClient.close(pay.getOrderNo());
        } catch (Exception e) {
            log.warn("关闭支付宝单失败 orderNo={}: {}", pay.getOrderNo(), e.getMessage());
        }
        pay.setPayStatus(PayStatusEnum.CLOSED.getValue());
        pay.setCloseTime(LocalDateTime.now());
        payOrderDao.updateById(pay);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<MallAlipayPayVO> prepay(MallWechatPrepayForm form) {
        if (!isReady()) {
            return ResponseDTO.userErrorParam("支付宝支付未启用，请使用收款码转账");
        }
        MallOrderEntity order = mallOrderService.requireOwnOrderForPay(form.getOrderId());
        if (!MallOrderStatusEnum.WAIT_PAY.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("当前订单不是待付款");
        }
        String type = StringUtils.defaultIfBlank(form.getTradeType(), "native").toLowerCase();
        Integer tradeType = "h5".equals(type) || "wap".equals(type)
                ? PayTradeTypeEnum.H5.getValue() : PayTradeTypeEnum.NATIVE.getValue();
        PayOrderEntity pay = ensurePayOrder(order, tradeType);
        MallAlipayPayVO vo = new MallAlipayPayVO();
        vo.setAlipayReady(true);
        vo.setMock(alipayClient.isMock());
        vo.setTradeType(type);
        vo.setPayOrderId(pay.getPayOrderId());
        String amount = fenToYuan(pay.getAmount()).toPlainString();
        try {
            if (PayTradeTypeEnum.H5.equalsValue(tradeType)) {
                alipayClient.wapForm(pay.getOrderNo(), pay.getDescription(), amount, null);
            } else {
                String codeUrl = alipayClient.precreate(pay.getOrderNo(), pay.getDescription(), amount, null);
                pay.setCodeUrl(codeUrl);
                vo.setCodeUrl(codeUrl);
                vo.setQrcodeBase64("data:image/png;base64," + Base64.encode(QrCodeUtil.generatePng(codeUrl, 280, 280)));
            }
        } catch (BusinessException e) {
            return ResponseDTO.userErrorParam(e.getMessage());
        }
        pay.setTradeType(tradeType);
        payOrderDao.updateById(pay);
        order.setPayChannel(30);
        mallOrderDao.updateById(order);
        return ResponseDTO.ok(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> mockPay(Long orderId) {
        if (!alipayClient.isMock()) {
            return ResponseDTO.userErrorParam("仅演示模式可模拟支付");
        }
        MallOrderEntity order = mallOrderService.requireOwnOrderForPay(orderId);
        PayOrderEntity pay = ensurePayOrder(order, PayTradeTypeEnum.NATIVE.getValue());
        if (!PayStatusEnum.WAIT_PAY.equalsValue(pay.getPayStatus())) {
            return ResponseDTO.userErrorParam("支付单不是待支付");
        }
        pay.setPayStatus(PayStatusEnum.SUCCESS.getValue());
        pay.setTransactionId("MOCKALI" + RandomStringUtils.randomNumeric(12));
        pay.setSuccessTime(LocalDateTime.now());
        pay.setPayerTotal(pay.getAmount());
        payOrderDao.updateById(pay);
        mallOrderService.freezeCommissionPublic(applyPaid(order, pay.getTransactionId()));
        return ResponseDTO.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> refundClose(Long orderId, String reason) {
        if (!isReady()) {
            return ResponseDTO.userErrorParam("支付宝支付未启用");
        }
        MallOrderEntity order = mallOrderDao.selectById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("订单不存在");
        }
        if (!Integer.valueOf(30).equals(order.getPayChannel())) {
            return ResponseDTO.userErrorParam("不是支付宝支付订单");
        }
        if (MallOrderStatusEnum.CLOSED.equalsValue(order.getOrderStatus())
                || MallOrderStatusEnum.COMPLETED.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("当前状态不能退款关单");
        }
        PayOrderEntity pay = findByMall(order);
        if (pay == null) {
            return ResponseDTO.userErrorParam("找不到对应支付单");
        }
        String remark = StringUtils.defaultIfBlank(StringUtils.trimToNull(reason), "商家支付宝退款关单");
        if (PayStatusEnum.SUCCESS.equalsValue(pay.getPayStatus()) || PayStatusEnum.REFUNDING.equalsValue(pay.getPayStatus())) {
            String refundNo = "RF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + RandomStringUtils.randomNumeric(4);
            if (!alipayClient.isMock()) {
                alipayClient.refund(pay.getOrderNo(), refundNo,
                        fenToYuan(pay.getAmount()).toPlainString(), remark);
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

    private MallOrderEntity applyPaid(MallOrderEntity order, String transactionId) {
        order.setOrderStatus(MallOrderStatusEnum.WAIT_SHIP.getValue());
        order.setPayStatus(MallPayStatusEnum.PAID.getValue());
        order.setPayTime(LocalDateTime.now());
        order.setPayChannel(30);
        order.setWxTransactionId(transactionId);
        mallOrderDao.updateById(order);
        return order;
    }

    private PayOrderEntity ensurePayOrder(MallOrderEntity order, Integer tradeType) {
        PayOrderEntity exist = findByMall(order);
        if (exist != null) {
            return exist;
        }
        PayOrderEntity entity = new PayOrderEntity();
        entity.setOrderNo("ALI" + order.getOrderNo());
        entity.setMallOrderId(order.getOrderId());
        entity.setPayChannel(PayChannelEnum.ALIPAY.getValue());
        entity.setDescription(StringUtils.abbreviate(order.getGoodsName(), 120));
        entity.setAmount(order.getAmount().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue());
        entity.setTradeType(tradeType);
        entity.setPayStatus(PayStatusEnum.WAIT_PAY.getValue());
        entity.setRefundAmount(0);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        payOrderDao.insert(entity);
        return entity;
    }

    private PayOrderEntity findByMall(MallOrderEntity order) {
        return payOrderDao.selectOne(new LambdaQueryWrapper<PayOrderEntity>()
                .eq(PayOrderEntity::getMallOrderId, order.getOrderId())
                .eq(PayOrderEntity::getPayChannel, PayChannelEnum.ALIPAY.getValue())
                .eq(PayOrderEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
    }

    private BigDecimal fenToYuan(Integer fen) {
        return new BigDecimal(fen).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
