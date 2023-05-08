package cn.biq.mn.user.group;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;
import cn.biq.mn.base.base.IdAndNameDetails;

@Getter @Setter
public class GroupDetails extends BaseDetails {

    private String name;
    private String notes;
    private Boolean enable;
    private String defaultCurrencyCode;
    private String role;
    private boolean isDefault;

}
