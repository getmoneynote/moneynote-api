package cn.biq.mn.admin.rbac.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;

@Getter @Setter
public class AdminAddForm {

    @NotBlank
    @NameField
    private String username;

}
