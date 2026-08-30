package net.lab1024.sa.admin.module.business.mall.domain.vo;

import lombok.Data;

@Data
public class MallAddressVO {

    private Long addressId;
    private Long memberId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Boolean defaultFlag;
}
