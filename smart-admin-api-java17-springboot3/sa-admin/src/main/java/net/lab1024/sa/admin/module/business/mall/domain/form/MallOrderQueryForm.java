package net.lab1024.sa.admin.module.business.mall.domain.form;

import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

@Data
public class MallOrderQueryForm extends PageParam {

    private String orderNo;

    private Integer orderStatus;

    private String waybillNo;

    private String phone;
}
