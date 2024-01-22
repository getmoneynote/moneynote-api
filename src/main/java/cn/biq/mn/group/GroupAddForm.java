package cn.biq.mn.group;

import cn.biq.mn.validation.AccountCurrencyCodeField;
import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupAddForm {

    @NotBlank
    @NameField
    private String name;

    @NotBlank
    @AccountCurrencyCodeField
    private String defaultCurrencyCode;

    @NotesField
    private String notes;

    @NotNull
    private Integer templateId; //账本模板id，新建组默认生成一个账本

}
