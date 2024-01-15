package cn.biq.mn.user;

import cn.biq.mn.validation.PasswordField;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class ChangePasswordRequest {

    @PasswordField
    private String oldPassword;

    @PasswordField
    private String newPassword;

}
