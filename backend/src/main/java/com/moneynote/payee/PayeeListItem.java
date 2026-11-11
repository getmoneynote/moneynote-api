package com.moneynote.payee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayeeListItem {

    private Long id;
    private String name;
    private String notes;
    private Boolean canExpense;
    private Boolean canIncome;

}
