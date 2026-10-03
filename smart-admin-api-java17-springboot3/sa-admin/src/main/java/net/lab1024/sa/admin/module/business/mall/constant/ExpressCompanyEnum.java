package net.lab1024.sa.admin.module.business.mall.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 常用快递公司，code 与快递100 查询接口的 com 一致。
 */
@AllArgsConstructor
@Getter
public enum ExpressCompanyEnum implements BaseEnum {

    SF("shunfeng", "顺丰速运"),
    YTO("yuantong", "圆通速递"),
    ZTO("zhongtong", "中通快递"),
    YD("yunda", "韵达快递"),
    STO("shentong", "申通快递"),
    EMS("ems", "EMS"),
    JD("jd", "京东物流"),
    JT("jtexpress", "极兔速递"),
    DB("debangwuliu", "德邦快递"),
    YZ("youzhengguonei", "邮政包裹"),
    ;

    private final String value;
    private final String desc;

    public static ExpressCompanyEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ExpressCompanyEnum item : values()) {
            if (item.value.equalsIgnoreCase(code)) {
                return item;
            }
        }
        return null;
    }
}
