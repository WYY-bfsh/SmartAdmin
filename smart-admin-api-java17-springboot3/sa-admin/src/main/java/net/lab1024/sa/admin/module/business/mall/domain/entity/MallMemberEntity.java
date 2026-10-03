package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_mall_member")
public class MallMemberEntity {

    @TableId(type = IdType.AUTO)
    private Long memberId;

    private String phone;

    private String nickname;

    private String password;

    private String inviteCode;

    private Long parentMemberId;

    private String avatar;

    private String wechatPayQr;

    private String wechatReceiveQr;

    private String wechatOpenid;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
