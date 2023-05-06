package cn.biq.mn.admin.booktemplate.book;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import cn.biq.mn.base.validation.NameField;

@Getter @Setter
public class BookAddForm {

    @NotBlank
    @NameField
    private String name;

    private String notes;

    @URL
    private String previewUrl;

}
