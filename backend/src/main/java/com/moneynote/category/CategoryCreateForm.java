package com.moneynote.category;

import com.moneynote.validate.ValidName;
import com.moneynote.validate.ValidNotes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryCreateForm {

    @NotNull
    @Min(1)
    @Max(2)
    private Integer type;

    @ValidName
    private String name;

    @ValidNotes
    private String notes;

    private Long parentId;

}
