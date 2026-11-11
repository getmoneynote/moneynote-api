package com.moneynote.account;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AccountListItem {

    private Long id;
    private String name;
    private Integer type;
    private String notes;
    private String no;
    private Long balance;
    private Boolean include;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransferFrom;
    private Boolean canTransferTo;
    private Long creditLimit;

    /** 剩余额度 = creditLimit + balance（信用/贷款账户余额为负，额度加余额即可用额度），creditLimit 为空时为 null */
    private Long remainLimit;

    private Integer billDay;
    private BigDecimal apr;
    private Integer displayOrder;
    private Long currencyId;
    private String currencyName;
    private String currencySymbol;
    private Integer currencyDecimalPlaces;
    /** 是否为本位币（后端直接判定的事实，前端据此决定是否展示换算值） */
    private Boolean isBaseCurrency;
    private Long baseBalance;
    private String baseCurrencySymbol;
    private Integer baseCurrencyDecimalPlaces;

}
