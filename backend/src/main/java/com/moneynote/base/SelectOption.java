package com.moneynote.base;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SelectOption {

    private Long value;
    private String label;

    public String getTitle() {
        return label;
    }
}
