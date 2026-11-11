package com.moneynote.base;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IdAndNameDetails extends BaseDetails {

    private String name;

    public String getLabel() {
        return name;
    }

    public Long getValue() {
        return getId();
    }

    public String getTitle() {
        return name;
    }

    public IdAndNameDetails() { }

    public IdAndNameDetails(Long id, String name) {
        this.setId(id);
        this.name = name;
    }

}
