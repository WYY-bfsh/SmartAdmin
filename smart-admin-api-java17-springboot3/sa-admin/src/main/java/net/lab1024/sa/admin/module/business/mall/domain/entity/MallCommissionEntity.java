package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_mall_commission")
public class MallCommissionEntity {

    @TableId(type = IdType.AUTO)
    private Long commissionId;

    private Long memberId;

    private Long fromMemberId;

    private Long orderId;

    private String orderNo;

    private BigDecimal amount;

    private BigDecimal rate;

    /**
     * 1 一级直推  2 二级粉丝
     */
    private Integer commissionLevel;

    private Integer status;

    private LocalDateTime settleTime;

    private LocalDateTime createTime;
}
