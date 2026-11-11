package com.moneynote.currency;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CurrencyListItem {

    private Long id;
    private String name;
    private String code;
    private String symbol;
    private Integer decimalPlaces;
    private BigDecimal usdRate;
    private BigDecimal baseRate;
    private Boolean isBase;
    private Boolean deletable;
    private Boolean rateEditable;

}
