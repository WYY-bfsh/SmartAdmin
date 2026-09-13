package net.lab1024.sa.admin.module.business.pay;

import net.lab1024.sa.admin.module.business.pay.constant.PayReconBizTypeEnum;
import net.lab1024.sa.admin.module.business.pay.service.PayBillLine;
import net.lab1024.sa.admin.module.business.pay.service.PayBillParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 账单解析
 */
class PayBillParserTest {

    private final PayBillParser parser = new PayBillParser();

    @Test
    void parseAlipayTrade() {
        String csv = """
                支付宝交易号,商户订单号,业务类型,订单金额（元）,完成时间
                20240913001,ALI20240913001,交易,1.00,2024-09-12 10:00:00
                """;
        List<PayBillLine> lines = parser.parse(csv.getBytes(StandardCharsets.UTF_8), "alipay.csv");
        Assertions.assertEquals(1, lines.size());
        Assertions.assertEquals("ALI20240913001", lines.get(0).getOrderNo());
        Assertions.assertEquals(100, lines.get(0).getAmountFen());
        Assertions.assertEquals(PayReconBizTypeEnum.TRADE.getValue(), lines.get(0).getBizType());
    }

    @Test
    void parseWechatSuccessNotRefund() {
        String csv = """
                交易时间,微信订单号,商户订单号,交易状态,订单金额,微信退款单号,商户退款单号,退款金额
                2024-09-12 10:00:00,42000001,WX20240913001,SUCCESS,1.00,0,0,0.00
                """;
        List<PayBillLine> lines = parser.parse(csv.getBytes(StandardCharsets.UTF_8), "wechat.csv");
        Assertions.assertEquals(1, lines.size());
        Assertions.assertEquals(PayReconBizTypeEnum.TRADE.getValue(), lines.get(0).getBizType());
        Assertions.assertEquals(100, lines.get(0).getAmountFen());
    }
}
