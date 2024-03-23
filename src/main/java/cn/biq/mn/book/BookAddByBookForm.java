package cn.biq.mn.book;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookAddByBookForm {

    @NotNull
    private Integer bookId;

    @Valid
    @NotNull
    private BookAddForm book;

}
