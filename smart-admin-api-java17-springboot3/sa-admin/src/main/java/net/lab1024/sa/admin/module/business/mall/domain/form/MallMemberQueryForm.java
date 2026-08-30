package net.lab1024.sa.admin.module.business.mall.domain.form;

import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

@Data
public class MallMemberQueryForm extends PageParam {

    private String phone;

    private String inviteCode;
}
