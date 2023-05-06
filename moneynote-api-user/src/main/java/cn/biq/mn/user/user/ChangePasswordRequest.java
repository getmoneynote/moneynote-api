package cn.biq.mn.user.user;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.PasswordField;


@Getter @Setter
public class ChangePasswordRequest {

    @PasswordField
    private String oldPassword;

    @PasswordField
    private String newPassword;

}
