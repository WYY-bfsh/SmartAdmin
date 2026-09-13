package net.lab1024.sa.admin.module.business.pay.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 对账明细导出
 */
@Data
public class PayReconItemExcelVO {

    @ExcelProperty("匹配结果")
    private String matchStatus;

    @ExcelProperty("业务类型")
    private String bizType;

    @ExcelProperty("商户订单号")
    private String orderNo;

    @ExcelProperty("本地金额(元)")
    private BigDecimal localAmountYuan;

    @ExcelProperty("本地状态")
    private String localStatus;

    @ExcelProperty("渠道金额(元)")
    private BigDecimal channelAmountYuan;

    @ExcelProperty("渠道状态")
    private String channelStatus;

    @ExcelProperty("渠道交易号")
    private String channelTradeNo;

    @ExcelProperty("差额(元)")
    private BigDecimal diffAmountYuan;

    @ExcelProperty("已核销")
    private String handledFlag;

    @ExcelProperty("说明")
    private String remark;
}
