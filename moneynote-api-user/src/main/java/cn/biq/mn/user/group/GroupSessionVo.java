package cn.biq.mn.user.group;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;

@Getter @Setter
public class GroupSessionVo extends BaseDetails {

    private String name;
    private String defaultCurrencyCode;

}
