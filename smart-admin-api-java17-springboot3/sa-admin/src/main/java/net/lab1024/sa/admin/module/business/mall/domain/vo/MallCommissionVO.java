package net.lab1024.sa.admin.module.business.mall.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MallCommissionVO {

    private Long commissionId;
    private Long memberId;
    private String memberPhone;
    private Long fromMemberId;
    private String fromMemberPhone;
    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private BigDecimal rate;
    private Integer commissionLevel;
    private Integer status;
    private LocalDateTime settleTime;
    private LocalDateTime createTime;
}
