package net.lab1024.sa.admin.module.business.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_mall_address")
public class MallAddressEntity {

    @TableId(type = IdType.AUTO)
    private Long addressId;

    private Long memberId;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detail;

    private Boolean defaultFlag;

    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
