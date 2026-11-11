package com.moneynote.tag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagListItem {

    private Long id;
    private String name;
    private String notes;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;

}
