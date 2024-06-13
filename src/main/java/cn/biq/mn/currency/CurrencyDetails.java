package cn.biq.mn.currency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyDetails {

    private Integer id;
    private String name;
    private String description;
    private Double rate;
    private Double rate2; // for base

    public CurrencyDetails() { }

    public CurrencyDetails(Integer id, String name, Double rate) {
        this.id = id;
        this.name = name;
        this.rate = rate;
    }

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
