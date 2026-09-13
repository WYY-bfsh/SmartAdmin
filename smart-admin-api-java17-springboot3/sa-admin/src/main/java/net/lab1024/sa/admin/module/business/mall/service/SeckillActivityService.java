package net.lab1024.sa.admin.module.business.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.mall.config.MallProperties;
import net.lab1024.sa.admin.module.business.mall.dao.MallSettingDao;
import net.lab1024.sa.admin.module.business.mall.dao.SeckillActivityDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallSettingEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallSettingForm;
import net.lab1024.sa.admin.module.business.mall.domain.entity.SeckillActivityEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.SeckillActivityForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.SeckillActivityQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallConfigVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.SeckillActivityVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.module.support.redis.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SeckillActivityService {

    private static final DateTimeFormatter SEC = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private MallSeedService mallSeedService;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private MallSettingDao mallSettingDao;

    @Resource
    private MallProperties mallProperties;

    @Resource
    private RedisService redisService;

    @Resource
    private MallWechatPayService mallWechatPayService;

    @Resource
    private MallAlipayPayService mallAlipayPayService;

    public MallConfigVO config() {
        mallSeedService.ensureReady();
        MallConfigVO vo = new MallConfigVO();
        vo.setConcurrentLimit(mallProperties.getSeckill().getConcurrentLimit());
        vo.setPayTimeoutMinutes(mallProperties.getSeckill().getPayTimeoutMinutes());
        vo.setDefaultCommissionRate(mallProperties.getSeckill().getDefaultCommissionRate());
        vo.setKuaidi100Enabled(StringUtils.isNotBlank(mallProperties.getExpress().getKuaidi100Key())
                && StringUtils.isNotBlank(mallProperties.getExpress().getKuaidi100Customer()));
        vo.setH5Path(StringUtils.defaultIfBlank(mallProperties.getH5Path(), "http://175.27.131.7:8080/app/#/pages/mall/index"));
        fillMerchantQr(vo);
        mallWechatPayService.fillConfig(vo);
        mallAlipayPayService.fillConfig(vo);
        return vo;
    }

    public boolean hasMerchantPayQr() {
        MallConfigVO vo = config();
        return StringUtils.isNotBlank(vo.getMerchantWechatQr()) || StringUtils.isNotBlank(vo.getMerchantAlipayQr());
    }

    /**
     * 保存待付款页展示的商家微信/支付宝收款码。
     */
    public ResponseDTO<String> saveSetting(MallSettingForm form) {
        mallSeedService.ensureReady();
        MallSettingEntity entity = requireSetting();
        entity.setMerchantWechatQr(StringUtils.trimToNull(form.getMerchantWechatQr()));
        entity.setMerchantAlipayQr(StringUtils.trimToNull(form.getMerchantAlipayQr()));
        mallSettingDao.updateById(entity);
        return ResponseDTO.ok();
    }

    private void fillMerchantQr(MallConfigVO vo) {
        MallSettingEntity setting = requireSetting();
        vo.setMerchantWechatQr(setting.getMerchantWechatQr());
        vo.setMerchantAlipayQr(setting.getMerchantAlipayQr());
    }

    private MallSettingEntity requireSetting() {
        mallSeedService.ensureReady();
        MallSettingEntity entity = mallSettingDao.selectById(1L);
        if (entity != null) {
            return entity;
        }
        entity = new MallSettingEntity();
        entity.setSettingId(1L);
        entity.setCreateTime(LocalDateTime.now());
        mallSettingDao.insert(entity);
        return entity;
    }

    public ResponseDTO<PageResult<SeckillActivityVO>> query(SeckillActivityQueryForm queryForm) {
        mallSeedService.ensureReady();
        Page<SeckillActivityEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<SeckillActivityEntity> wrapper = new LambdaQueryWrapper<SeckillActivityEntity>()
                .eq(SeckillActivityEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getGoodsName()), SeckillActivityEntity::getGoodsName, queryForm.getGoodsName())
                .eq(queryForm.getEnabledFlag() != null, SeckillActivityEntity::getEnabledFlag, queryForm.getEnabledFlag())
                .orderByDesc(SeckillActivityEntity::getActivityId);
        seckillActivityDao.selectPage(page, wrapper);
        List<SeckillActivityVO> list = page.getRecords().stream().map(this::toVo).toList();
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<List<SeckillActivityVO>> listForH5() {
        mallSeedService.ensureReady();
        List<SeckillActivityEntity> list = seckillActivityDao.selectList(new LambdaQueryWrapper<SeckillActivityEntity>()
                .eq(SeckillActivityEntity::getDeletedFlag, false)
                .eq(SeckillActivityEntity::getEnabledFlag, true)
                .orderByAsc(SeckillActivityEntity::getStartTime));
        return ResponseDTO.ok(list.stream().map(this::toVo).toList());
    }

    public ResponseDTO<SeckillActivityVO> detail(Long activityId) {
        mallSeedService.ensureReady();
        SeckillActivityEntity entity = seckillActivityDao.selectById(activityId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("活动不存在");
        }
        return ResponseDTO.ok(toVo(entity));
    }

    public ResponseDTO<String> save(SeckillActivityForm form) {
        mallSeedService.ensureReady();
        if (form.getEndTime().isBefore(form.getStartTime())) {
            return ResponseDTO.userErrorParam("结束时间必须晚于开始时间");
        }
        if (form.getActivityId() == null) {
            SeckillActivityEntity entity = SmartBeanUtil.copy(form, SeckillActivityEntity.class);
            entity.setSoldCount(0);
            entity.setDeletedFlag(Boolean.FALSE);
            entity.setEnabledFlag(form.getEnabledFlag() == null || form.getEnabledFlag());
            if (entity.getPerLimit() == null) {
                entity.setPerLimit(1);
            }
            entity.setCreateTime(LocalDateTime.now());
            seckillActivityDao.insert(entity);
        } else {
            SeckillActivityEntity entity = seckillActivityDao.selectById(form.getActivityId());
            if (entity == null) {
                return ResponseDTO.userErrorParam("活动不存在");
            }
            entity.setTitle(form.getTitle());
            entity.setGoodsName(form.getGoodsName());
            entity.setCoverUrl(form.getCoverUrl());
            entity.setDetail(form.getDetail());
            entity.setOriginPrice(form.getOriginPrice());
            entity.setSeckillPrice(form.getSeckillPrice());
            entity.setStock(form.getStock());
            entity.setPerLimit(form.getPerLimit());
            entity.setStartTime(form.getStartTime());
            entity.setEndTime(form.getEndTime());
            entity.setConcurrentLimit(form.getConcurrentLimit());
            entity.setCommissionRate(form.getCommissionRate());
            entity.setEnabledFlag(form.getEnabledFlag());
            seckillActivityDao.updateById(entity);
        }
        return ResponseDTO.ok();
    }

    public boolean tryEnterQueue(Long activityId, Integer limit) {
        int max = limit == null || limit <= 0 ? mallProperties.getSeckill().getConcurrentLimit() : limit;
        String key = "mall:seckill:qps:" + activityId + ":" + LocalDateTime.now().format(SEC);
        try {
            String current = redisService.get(key);
            int count = current == null ? 0 : Integer.parseInt(current);
            if (count >= max) {
                return false;
            }
            redisService.set(key, String.valueOf(count + 1), 2);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public SeckillActivityVO toVo(SeckillActivityEntity entity) {
        SeckillActivityVO vo = SmartBeanUtil.copy(entity, SeckillActivityVO.class);
        if (vo.getConcurrentLimit() == null) {
            vo.setConcurrentLimit(mallProperties.getSeckill().getConcurrentLimit());
        }
        if (vo.getCommissionRate() == null) {
            vo.setCommissionRate(mallProperties.getSeckill().getDefaultCommissionRate());
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(entity.getStartTime())) {
            vo.setSaleStatus(10);
            vo.setSaleStatusDesc("未开始");
            vo.setRemainSeconds(Duration.between(now, entity.getStartTime()).getSeconds());
        } else if (now.isAfter(entity.getEndTime()) || entity.getStock() == null || entity.getStock() <= 0) {
            vo.setSaleStatus(30);
            vo.setSaleStatusDesc(entity.getStock() != null && entity.getStock() <= 0 ? "已售罄" : "已结束");
            vo.setRemainSeconds(0L);
        } else {
            vo.setSaleStatus(20);
            vo.setSaleStatusDesc("进行中");
            vo.setRemainSeconds(Duration.between(now, entity.getEndTime()).getSeconds());
        }
        return vo;
    }
}
