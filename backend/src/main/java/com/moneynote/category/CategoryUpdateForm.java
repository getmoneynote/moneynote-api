package com.moneynote.category;

import com.moneynote.validate.ValidName;
import com.moneynote.validate.ValidNotes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateForm {

    @ValidName
    private String name;

    @ValidNotes
    private String notes;

    /**
     * 目标父级分类 ID，null 表示移动到根级
     */
    private Long parentId;

}
