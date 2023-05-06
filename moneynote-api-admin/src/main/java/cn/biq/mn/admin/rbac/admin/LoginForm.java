package cn.biq.mn.admin.rbac.admin;


import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.PasswordField;
import cn.biq.mn.base.validation.UserNameField;

@Getter @Setter
public class LoginForm {

    @UserNameField
    private String username;

    @PasswordField
    private String password;

    private Boolean remember = false;

}
