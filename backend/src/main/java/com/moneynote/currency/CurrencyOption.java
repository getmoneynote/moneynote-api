package com.moneynote.currency;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CurrencyOption {

    private Long value;
    private String label;
    private String code;
    private String symbol;
    private Integer decimalPlaces;

    public String getTitle() {
        return label;
    }

}
