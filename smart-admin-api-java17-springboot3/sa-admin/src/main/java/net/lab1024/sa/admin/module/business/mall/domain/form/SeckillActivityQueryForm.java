package net.lab1024.sa.admin.module.business.mall.domain.form;

import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

@Data
public class SeckillActivityQueryForm extends PageParam {

    private String goodsName;

    private Boolean enabledFlag;
}
