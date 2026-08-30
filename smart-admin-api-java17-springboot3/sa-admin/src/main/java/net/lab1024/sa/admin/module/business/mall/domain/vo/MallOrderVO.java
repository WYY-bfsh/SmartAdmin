package net.lab1024.sa.admin.module.business.mall.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MallOrderVO {

    private Long orderId;
    private String orderNo;
    private Long memberId;
    private String memberPhone;
    private Long activityId;
    private String goodsName;
    private String coverUrl;
    private Integer qty;
    private BigDecimal price;
    private BigDecimal amount;
    private Integer payStatus;
    private Integer orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String expressCode;
    private String expressName;
    private String waybillNo;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private List<MallExpressTraceVO> traces;
}
