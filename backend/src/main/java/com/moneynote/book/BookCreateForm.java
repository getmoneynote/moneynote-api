package com.moneynote.book;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.validate.ValidName;

@Getter @Setter
public class BookCreateForm {

    @ValidName
    private String name;

    /**
     * 记账币种，必须属于当前用户，创建后不可修改
     */
    @NotNull
    private Long currencyId;

    @Size(max = 1024)
    private String notes;

}
