package cn.biq.mn.group;

import cn.biq.mn.base.BaseDetails;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupSessionVo extends BaseDetails {

    private String name;
    private String defaultCurrencyCode;

}
