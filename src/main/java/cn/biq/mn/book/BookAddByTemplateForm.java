package cn.biq.mn.book;

import cn.biq.mn.validation.NameField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookAddByTemplateForm {

    @NotNull
    private Integer templateId;

    @NameField
    private String bookName;

}
