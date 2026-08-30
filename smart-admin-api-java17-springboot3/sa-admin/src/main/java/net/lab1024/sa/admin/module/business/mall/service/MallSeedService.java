package net.lab1024.sa.admin.module.business.mall.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.mall.dao.MallAddressDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallMemberDao;
import net.lab1024.sa.admin.module.business.mall.dao.SeckillActivityDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallAddressEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallMemberEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.SeckillActivityEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class MallSeedService {

    @Resource
    private MallSchemaService mallSchemaService;

    @Resource
    private MallMemberDao mallMemberDao;

    @Resource
    private MallAddressDao mallAddressDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    private volatile boolean ready = false;

    public void ensureReady() {
        mallSchemaService.ensureTables();
        if (ready) {
            return;
        }
        synchronized (this) {
            if (ready) {
                return;
            }
            seedMembers();
            seedActivities();
            ready = true;
        }
    }

    private void seedMembers() {
        if (mallMemberDao.selectCount(new LambdaQueryWrapper<MallMemberEntity>()) > 0) {
            return;
        }
        MallMemberEntity parent = new MallMemberEntity();
        parent.setPhone("13800000001");
        parent.setNickname("演示店长");
        parent.setPassword(DigestUtil.md5Hex("123456"));
        parent.setInviteCode("SA0001");
        parent.setDeletedFlag(Boolean.FALSE);
        parent.setCreateTime(LocalDateTime.now());
        mallMemberDao.insert(parent);

        MallMemberEntity child = new MallMemberEntity();
        child.setPhone("13800000002");
        child.setNickname("演示买家");
        child.setPassword(DigestUtil.md5Hex("123456"));
        child.setInviteCode("SA0002");
        child.setParentMemberId(parent.getMemberId());
        child.setDeletedFlag(Boolean.FALSE);
        child.setCreateTime(LocalDateTime.now());
        mallMemberDao.insert(child);

        MallAddressEntity address = new MallAddressEntity();
        address.setMemberId(child.getMemberId());
        address.setReceiverName("王先生");
        address.setReceiverPhone("13800000002");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("科技园路1号演示小区 8栋1201");
        address.setDefaultFlag(Boolean.TRUE);
        address.setDeletedFlag(Boolean.FALSE);
        address.setCreateTime(LocalDateTime.now());
        mallAddressDao.insert(address);
    }

    private void seedActivities() {
        if (seckillActivityDao.selectCount(new LambdaQueryWrapper<SeckillActivityEntity>()
                .eq(SeckillActivityEntity::getDeletedFlag, false)) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        SeckillActivityEntity live = new SeckillActivityEntity();
        live.setTitle("今日秒杀");
        live.setGoodsName("蓝牙降噪耳机");
        live.setCoverUrl("https://picsum.photos/seed/seckill-earphone/600/600");
        live.setDetail("正规秒杀样例：限时限量、单人限购1件。付款后由商家真实发货，可填快递公司与运单号，轨迹结构对齐快递100。");
        live.setOriginPrice(new BigDecimal("299.00"));
        live.setSeckillPrice(new BigDecimal("99.00"));
        live.setStock(50);
        live.setSoldCount(3);
        live.setPerLimit(1);
        live.setStartTime(now.minusHours(1));
        live.setEndTime(now.plusDays(3));
        live.setConcurrentLimit(200);
        live.setCommissionRate(new BigDecimal("0.05"));
        live.setEnabledFlag(Boolean.TRUE);
        live.setDeletedFlag(Boolean.FALSE);
        live.setCreateTime(now);
        seckillActivityDao.insert(live);

        SeckillActivityEntity soon = new SeckillActivityEntity();
        soon.setTitle("明日预告");
        soon.setGoodsName("便携咖啡机");
        soon.setCoverUrl("https://picsum.photos/seed/seckill-coffee/600/600");
        soon.setDetail("即将开始的秒杀场次，可用于演示倒计时。");
        soon.setOriginPrice(new BigDecimal("599.00"));
        soon.setSeckillPrice(new BigDecimal("199.00"));
        soon.setStock(30);
        soon.setSoldCount(0);
        soon.setPerLimit(1);
        soon.setStartTime(now.plusHours(6));
        soon.setEndTime(now.plusDays(2));
        soon.setEnabledFlag(Boolean.TRUE);
        soon.setDeletedFlag(Boolean.FALSE);
        soon.setCreateTime(now);
        seckillActivityDao.insert(soon);
    }
}
