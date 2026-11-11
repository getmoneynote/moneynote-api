package com.moneynote.account;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AccountBalanceSummary {

    private Long total;
    private Long totalCreditLimit;
    private Long totalAvailable;
    private String currencySymbol;
    private Integer decimalPlaces;

}
