package com.moneynote.book;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookUpdateForm {

    @Size(max = 64)
    private String name;

    @Size(max = 1024)
    private String notes;

}
