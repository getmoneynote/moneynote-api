package cn.biq.mn.group;

import cn.biq.mn.base.BaseDetails;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupDetails extends BaseDetails {

    private String name;
    private String notes;
    private Boolean enable;
    private String defaultCurrencyCode;
    private String role;
    private boolean isDefault;
    private Integer roleId;

}
