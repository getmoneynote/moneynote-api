package com.moneynote.book;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookListItem {

    private Long id;
    private String name;
    private String notes;
    private Long currencyId;
    private String currencyName;
    private Boolean isCurrent;

}
