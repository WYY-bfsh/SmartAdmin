package net.lab1024.sa.admin.module.business.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.mall.config.MallProperties;
import net.lab1024.sa.admin.module.business.mall.constant.CommissionStatusEnum;
import net.lab1024.sa.admin.module.business.mall.constant.ExpressCompanyEnum;
import net.lab1024.sa.admin.module.business.mall.constant.MallOrderStatusEnum;
import net.lab1024.sa.admin.module.business.mall.constant.MallPayStatusEnum;
import net.lab1024.sa.admin.module.business.mall.dao.MallAddressDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallCommissionDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallMemberDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallOrderDao;
import net.lab1024.sa.admin.module.business.mall.dao.SeckillActivityDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallAddressEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallCommissionEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallMemberEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallOrderEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.SeckillActivityEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallCommissionQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallCreateOrderForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallOrderQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallShipForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallCommissionVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallOrderVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.SeckillActivityVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MallOrderService {

    private static final DateTimeFormatter ORDER_NO = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Resource
    private MallSeedService mallSeedService;

    @Resource
    private MallMemberService mallMemberService;

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private MallExpressService mallExpressService;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private MallOrderDao mallOrderDao;

    @Resource
    private MallAddressDao mallAddressDao;

    @Resource
    private MallCommissionDao mallCommissionDao;

    @Resource
    private MallMemberDao mallMemberDao;

    @Resource
    private MallProperties mallProperties;

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<MallOrderVO> create(MallCreateOrderForm form) {
        mallSeedService.ensureReady();
        MallMemberEntity member = mallMemberService.requireMember();
        SeckillActivityEntity activity = seckillActivityDao.selectById(form.getActivityId());
        if (activity == null || Boolean.TRUE.equals(activity.getDeletedFlag()) || !Boolean.TRUE.equals(activity.getEnabledFlag())) {
            return ResponseDTO.userErrorParam("活动不存在或已下架");
        }
        SeckillActivityVO sale = seckillActivityService.toVo(activity);
        if (sale.getSaleStatus() != 20) {
            return ResponseDTO.userErrorParam(sale.getSaleStatusDesc());
        }
        if (!seckillActivityService.tryEnterQueue(activity.getActivityId(), sale.getConcurrentLimit())) {
            return ResponseDTO.userErrorParam("当前抢购人数过多（同时人数上限 " + sale.getConcurrentLimit() + "），请稍后再试");
        }
        int bought = mallOrderDao.countValidByMemberActivity(member.getMemberId(), activity.getActivityId());
        int limit = activity.getPerLimit() == null ? 1 : activity.getPerLimit();
        if (bought + form.getQty() > limit) {
            return ResponseDTO.userErrorParam("超过每人限购 " + limit + " 件");
        }
        MallAddressEntity address = mallAddressDao.selectById(form.getAddressId());
        if (address == null || !address.getMemberId().equals(member.getMemberId()) || Boolean.TRUE.equals(address.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("请选择有效收货地址");
        }
        int rows = seckillActivityDao.deductStock(activity.getActivityId(), form.getQty());
        if (rows <= 0) {
            return ResponseDTO.userErrorParam("库存不足");
        }
        MallOrderEntity order = new MallOrderEntity();
        order.setOrderNo("SK" + LocalDateTime.now().format(ORDER_NO) + RandomStringUtils.randomNumeric(3));
        order.setMemberId(member.getMemberId());
        order.setActivityId(activity.getActivityId());
        order.setGoodsName(activity.getGoodsName());
        order.setCoverUrl(activity.getCoverUrl());
        order.setQty(form.getQty());
        order.setPrice(activity.getSeckillPrice());
        order.setAmount(activity.getSeckillPrice().multiply(BigDecimal.valueOf(form.getQty())).setScale(2, RoundingMode.HALF_UP));
        order.setPayStatus(MallPayStatusEnum.WAIT_PAY.getValue());
        order.setOrderStatus(MallOrderStatusEnum.WAIT_PAY.getValue());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(joinAddress(address));
        order.setParentMemberId(member.getParentMemberId());
        order.setExpireTime(LocalDateTime.now().plusMinutes(mallProperties.getSeckill().getPayTimeoutMinutes()));
        order.setDeletedFlag(Boolean.FALSE);
        order.setCreateTime(LocalDateTime.now());
        mallOrderDao.insert(order);
        return ResponseDTO.ok(toVo(order, false));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<MallOrderVO> mockPay(Long orderId) {
        MallOrderEntity order = requireOwnOrder(orderId);
        if (!MallOrderStatusEnum.WAIT_PAY.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("当前订单不是待付款状态");
        }
        order.setPayStatus(MallPayStatusEnum.PAID.getValue());
        order.setOrderStatus(MallOrderStatusEnum.WAIT_SHIP.getValue());
        order.setPayTime(LocalDateTime.now());
        mallOrderDao.updateById(order);
        freezeCommission(order);
        return ResponseDTO.ok(toVo(order, false));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> ship(MallShipForm form) {
        mallSeedService.ensureReady();
        MallOrderEntity order = mallOrderDao.selectById(form.getOrderId());
        if (order == null || Boolean.TRUE.equals(order.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("订单不存在");
        }
        if (!MallOrderStatusEnum.WAIT_SHIP.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("只有待发货订单可以发货");
        }
        ExpressCompanyEnum company = ExpressCompanyEnum.getByCode(form.getExpressCode());
        if (company == null) {
            return ResponseDTO.userErrorParam("不支持的快递公司");
        }
        order.setExpressCode(company.getValue());
        order.setExpressName(company.getDesc());
        order.setWaybillNo(form.getWaybillNo().trim());
        order.setShipTime(LocalDateTime.now());
        order.setOrderStatus(MallOrderStatusEnum.SHIPPED.getValue());
        mallOrderDao.updateById(order);
        mallExpressService.initShipTraces(order);
        mallExpressService.refreshAndList(order);
        return ResponseDTO.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> confirmReceive(Long orderId) {
        MallOrderEntity order = requireOwnOrder(orderId);
        if (!MallOrderStatusEnum.SHIPPED.equalsValue(order.getOrderStatus())) {
            return ResponseDTO.userErrorParam("订单尚未发货");
        }
        completeOrder(order);
        return ResponseDTO.ok();
    }

    public ResponseDTO<MallOrderVO> detail(Long orderId, boolean admin) {
        mallSeedService.ensureReady();
        MallOrderEntity order = admin ? mallOrderDao.selectById(orderId) : requireOwnOrder(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("订单不存在");
        }
        if (MallOrderStatusEnum.SHIPPED.equalsValue(order.getOrderStatus())) {
            mallExpressService.refreshAndList(order);
        }
        return ResponseDTO.ok(toVo(order, true));
    }

    public ResponseDTO<PageResult<MallOrderVO>> queryAdmin(MallOrderQueryForm queryForm) {
        mallSeedService.ensureReady();
        Page<MallOrderEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<MallOrderEntity> wrapper = baseOrderWrapper(queryForm);
        mallOrderDao.selectPage(page, wrapper);
        List<MallOrderVO> list = page.getRecords().stream().map(item -> toVo(item, false)).toList();
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<List<MallOrderVO>> listMine(Integer orderStatus) {
        MallMemberEntity member = mallMemberService.requireMember();
        LambdaQueryWrapper<MallOrderEntity> wrapper = new LambdaQueryWrapper<MallOrderEntity>()
                .eq(MallOrderEntity::getMemberId, member.getMemberId())
                .eq(MallOrderEntity::getDeletedFlag, false)
                .eq(orderStatus != null, MallOrderEntity::getOrderStatus, orderStatus)
                .orderByDesc(MallOrderEntity::getOrderId);
        return ResponseDTO.ok(mallOrderDao.selectList(wrapper).stream().map(item -> toVo(item, false)).toList());
    }

    public ResponseDTO<PageResult<MallCommissionVO>> queryCommission(MallCommissionQueryForm queryForm, boolean mineOnly) {
        mallSeedService.ensureReady();
        Page<MallCommissionEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<MallCommissionEntity> wrapper = new LambdaQueryWrapper<MallCommissionEntity>()
                .like(StringUtils.isNotBlank(queryForm.getOrderNo()), MallCommissionEntity::getOrderNo, queryForm.getOrderNo())
                .eq(queryForm.getStatus() != null, MallCommissionEntity::getStatus, queryForm.getStatus())
                .orderByDesc(MallCommissionEntity::getCommissionId);
        if (mineOnly) {
            wrapper.eq(MallCommissionEntity::getMemberId, mallMemberService.requireMember().getMemberId());
        }
        mallCommissionDao.selectPage(page, wrapper);
        List<MallCommissionVO> list = page.getRecords().stream().map(this::toCommissionVo).toList();
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<List<MallMemberEntity>> myTeam() {
        MallMemberEntity member = mallMemberService.requireMember();
        return ResponseDTO.ok(mallMemberDao.selectList(new LambdaQueryWrapper<MallMemberEntity>()
                .eq(MallMemberEntity::getParentMemberId, member.getMemberId())
                .eq(MallMemberEntity::getDeletedFlag, false)));
    }

    public int closeExpired() {
        mallSeedService.ensureReady();
        List<MallOrderEntity> list = mallOrderDao.selectList(new LambdaQueryWrapper<MallOrderEntity>()
                .eq(MallOrderEntity::getOrderStatus, MallOrderStatusEnum.WAIT_PAY.getValue())
                .eq(MallOrderEntity::getDeletedFlag, false)
                .le(MallOrderEntity::getExpireTime, LocalDateTime.now()));
        int count = 0;
        for (MallOrderEntity order : list) {
            order.setOrderStatus(MallOrderStatusEnum.CLOSED.getValue());
            order.setPayStatus(MallPayStatusEnum.CLOSED.getValue());
            order.setCloseTime(LocalDateTime.now());
            mallOrderDao.updateById(order);
            seckillActivityDao.restoreStock(order.getActivityId(), order.getQty());
            count++;
        }
        return count;
    }

    private void completeOrder(MallOrderEntity order) {
        order.setOrderStatus(MallOrderStatusEnum.COMPLETED.getValue());
        order.setReceiveTime(LocalDateTime.now());
        mallOrderDao.updateById(order);
        MallCommissionEntity commission = mallCommissionDao.selectOne(new LambdaQueryWrapper<MallCommissionEntity>()
                .eq(MallCommissionEntity::getOrderId, order.getOrderId())
                .last("LIMIT 1"));
        if (commission != null && CommissionStatusEnum.FROZEN.equalsValue(commission.getStatus())) {
            commission.setStatus(CommissionStatusEnum.SETTLED.getValue());
            commission.setSettleTime(LocalDateTime.now());
            mallCommissionDao.updateById(commission);
        }
    }

    private void freezeCommission(MallOrderEntity order) {
        if (order.getParentMemberId() == null) {
            return;
        }
        Long exists = mallCommissionDao.selectCount(new LambdaQueryWrapper<MallCommissionEntity>()
                .eq(MallCommissionEntity::getOrderId, order.getOrderId()));
        if (exists != null && exists > 0) {
            return;
        }
        SeckillActivityEntity activity = seckillActivityDao.selectById(order.getActivityId());
        BigDecimal rate = activity != null && activity.getCommissionRate() != null
                ? activity.getCommissionRate()
                : mallProperties.getSeckill().getDefaultCommissionRate();
        MallCommissionEntity entity = new MallCommissionEntity();
        entity.setMemberId(order.getParentMemberId());
        entity.setFromMemberId(order.getMemberId());
        entity.setOrderId(order.getOrderId());
        entity.setOrderNo(order.getOrderNo());
        entity.setRate(rate);
        entity.setAmount(order.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP));
        entity.setStatus(CommissionStatusEnum.FROZEN.getValue());
        entity.setCreateTime(LocalDateTime.now());
        mallCommissionDao.insert(entity);
    }

    private MallOrderEntity requireOwnOrder(Long orderId) {
        MallMemberEntity member = mallMemberService.requireMember();
        MallOrderEntity order = mallOrderDao.selectById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeletedFlag()) || !order.getMemberId().equals(member.getMemberId())) {
            throw new net.lab1024.sa.base.common.exception.BusinessException("订单不存在");
        }
        return order;
    }

    private LambdaQueryWrapper<MallOrderEntity> baseOrderWrapper(MallOrderQueryForm queryForm) {
        LambdaQueryWrapper<MallOrderEntity> wrapper = new LambdaQueryWrapper<MallOrderEntity>()
                .eq(MallOrderEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getOrderNo()), MallOrderEntity::getOrderNo, queryForm.getOrderNo())
                .eq(queryForm.getOrderStatus() != null, MallOrderEntity::getOrderStatus, queryForm.getOrderStatus())
                .like(StringUtils.isNotBlank(queryForm.getWaybillNo()), MallOrderEntity::getWaybillNo, queryForm.getWaybillNo())
                .orderByDesc(MallOrderEntity::getOrderId);
        if (StringUtils.isNotBlank(queryForm.getPhone())) {
            List<MallMemberEntity> members = mallMemberDao.selectList(new LambdaQueryWrapper<MallMemberEntity>()
                    .like(MallMemberEntity::getPhone, queryForm.getPhone()));
            List<Long> ids = members.stream().map(MallMemberEntity::getMemberId).toList();
            if (ids.isEmpty()) {
                wrapper.eq(MallOrderEntity::getMemberId, -1L);
            } else {
                wrapper.in(MallOrderEntity::getMemberId, ids);
            }
        }
        return wrapper;
    }

    private MallOrderVO toVo(MallOrderEntity order, boolean withTrace) {
        MallOrderVO vo = SmartBeanUtil.copy(order, MallOrderVO.class);
        MallMemberEntity member = mallMemberDao.selectById(order.getMemberId());
        if (member != null) {
            vo.setMemberPhone(member.getPhone());
        }
        if (withTrace) {
            vo.setTraces(mallExpressService.list(order.getOrderId()));
        }
        return vo;
    }

    private MallCommissionVO toCommissionVo(MallCommissionEntity entity) {
        MallCommissionVO vo = SmartBeanUtil.copy(entity, MallCommissionVO.class);
        MallMemberEntity member = mallMemberDao.selectById(entity.getMemberId());
        MallMemberEntity from = mallMemberDao.selectById(entity.getFromMemberId());
        if (member != null) {
            vo.setMemberPhone(member.getPhone());
        }
        if (from != null) {
            vo.setFromMemberPhone(from.getPhone());
        }
        return vo;
    }

    private String joinAddress(MallAddressEntity address) {
        return StringUtils.defaultString(address.getProvince())
                + StringUtils.defaultString(address.getCity())
                + StringUtils.defaultString(address.getDistrict())
                + StringUtils.defaultString(address.getDetail());
    }
}
