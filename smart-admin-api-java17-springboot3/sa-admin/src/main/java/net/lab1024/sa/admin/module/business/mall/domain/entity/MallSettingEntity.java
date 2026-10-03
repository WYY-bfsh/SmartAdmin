package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商城全局配置（单行 setting_id=1），目前存放待付款页商家收款码。
 */
@Data
@TableName("t_mall_setting")
public class MallSettingEntity {

    @TableId(type = IdType.INPUT)
    private Long settingId;

    private String merchantWechatQr;

    private String merchantAlipayQr;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
