package cn.biq.mn.user.currency;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CurrencyDetails {

    private Integer id;
    private String name;
    private String description;
    private Double rate;

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
