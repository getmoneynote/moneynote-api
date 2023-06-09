package cn.biq.mn.admin.currency;

import cn.biq.mn.base.base.BaseDetails;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CurrencyDetails extends BaseDetails {

    private String name;
    private String description;
    private BigDecimal rate;
    private Boolean enable;

    public String getValue() {
        return name;
    }

    public String getLabel() {
        return name;
    }

    public String getTitle() {
        return name;
    }
}
