package net.lab1024.sa.admin.module.business.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "商城订单")
public class MallOrderVO {

    private Long orderId;
    private String orderNo;
    private Long memberId;
    private String memberPhone;
    private Long activityId;
    private String goodsName;
    private String coverUrl;
    @Schema(description = "用户付款截图")
    private String payProofUrl;
    @Schema(description = "用户付款说明")
    private String payNote;
    @Schema(description = "购买数量")
    private Integer qty;
    private BigDecimal price;
    private BigDecimal amount;
    @Schema(description = "支付状态 10待支付 15待确认 20已支付 30关闭")
    private Integer payStatus;
    @Schema(description = "订单状态 10待付款 15待商家确认 20待发货 30已发货 40已完成 50已关闭")
    private Integer orderStatus;
    @Schema(description = "备注，拒绝收款原因会写在这里")
    private String remark;
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
