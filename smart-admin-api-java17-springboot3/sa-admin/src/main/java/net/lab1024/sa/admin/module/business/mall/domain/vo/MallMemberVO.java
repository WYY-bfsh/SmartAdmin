package net.lab1024.sa.admin.module.business.mall.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MallMemberVO {

    private Long memberId;
    private String phone;
    private String nickname;
    private String avatar;
    private String wechatPayQr;
    private String wechatReceiveQr;
    private String inviteCode;
    private Long parentMemberId;
    private String parentPhone;
    private String token;
    private Integer teamCount;
    private Integer fanCount;
    private BigDecimal frozenCommission;
    private BigDecimal settledCommission;
    private LocalDateTime createTime;
}
