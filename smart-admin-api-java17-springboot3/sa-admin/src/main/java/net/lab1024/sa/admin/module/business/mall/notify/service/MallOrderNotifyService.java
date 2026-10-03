package net.lab1024.sa.admin.module.business.mall.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.mall.notify.dao.MallOrderNotifyDao;
import net.lab1024.sa.admin.module.business.mall.notify.domain.entity.MallOrderNotifyEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persist / upsert SMS notify rows (idempotent by orderNo + channel).
 */
@Service
public class MallOrderNotifyService {

    @Resource
    private MallOrderNotifyDao mallOrderNotifyDao;

    public MallOrderNotifyEntity findByOrderNoSms(String orderNo) {
        return mallOrderNotifyDao.selectOne(new LambdaQueryWrapper<MallOrderNotifyEntity>()
                .eq(MallOrderNotifyEntity::getOrderNo, orderNo)
                .eq(MallOrderNotifyEntity::getChannel, MallOrderNotifyEntity.CHANNEL_SMS)
                .last("LIMIT 1"));
    }

    public boolean isSuccess(String orderNo) {
        MallOrderNotifyEntity row = findByOrderNoSms(orderNo);
        return row != null && MallOrderNotifyEntity.STATUS_SUCCESS.equals(row.getStatus());
    }

    @Transactional(rollbackFor = Exception.class)
    public MallOrderNotifyEntity upsertPending(String orderNo, Long memberId, String eventId, String mobile, String content) {
        MallOrderNotifyEntity existing = findByOrderNoSms(orderNo);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            MallOrderNotifyEntity row = new MallOrderNotifyEntity();
            row.setOrderNo(orderNo);
            row.setMemberId(memberId);
            row.setChannel(MallOrderNotifyEntity.CHANNEL_SMS);
            row.setStatus(MallOrderNotifyEntity.STATUS_PENDING);
            row.setEventId(eventId);
            row.setMobile(mobile);
            row.setContent(content);
            row.setRetryCount(0);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            mallOrderNotifyDao.insert(row);
            return row;
        }
        existing.setMemberId(memberId);
        existing.setEventId(eventId);
        existing.setMobile(mobile);
        existing.setContent(content);
        if (!MallOrderNotifyEntity.STATUS_SUCCESS.equals(existing.getStatus())) {
            existing.setStatus(MallOrderNotifyEntity.STATUS_PENDING);
        }
        existing.setUpdateTime(now);
        mallOrderNotifyDao.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(Long id, String providerMsgId, String content) {
        MallOrderNotifyEntity row = mallOrderNotifyDao.selectById(id);
        if (row == null) {
            return;
        }
        row.setStatus(MallOrderNotifyEntity.STATUS_SUCCESS);
        row.setProviderMsgId(providerMsgId);
        row.setContent(content);
        row.setErrorMsg(null);
        row.setUpdateTime(LocalDateTime.now());
        mallOrderNotifyDao.updateById(row);
    }

    @Transactional(rollbackFor = Exception.class)
    public MallOrderNotifyEntity markFailedAndIncRetry(Long id, String errorMsg) {
        MallOrderNotifyEntity row = mallOrderNotifyDao.selectById(id);
        if (row == null) {
            return null;
        }
        int retry = row.getRetryCount() == null ? 0 : row.getRetryCount();
        row.setRetryCount(retry + 1);
        row.setStatus(MallOrderNotifyEntity.STATUS_FAILED);
        row.setErrorMsg(truncate(errorMsg, 500));
        row.setUpdateTime(LocalDateTime.now());
        mallOrderNotifyDao.updateById(row);
        return row;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
