package cn.biq.mn.user.book;

import cn.biq.mn.base.validation.NameField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookAddByBookForm {

    @NotNull
    private Integer bookId;

    @NameField
    private String bookName;

}
