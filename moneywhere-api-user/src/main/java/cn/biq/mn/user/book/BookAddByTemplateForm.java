package cn.biq.mn.user.book;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;

@Getter
@Setter
public class BookAddByTemplateForm {

    @NotNull
    private Integer templateId;

    @NameField
    private String bookName;

}
