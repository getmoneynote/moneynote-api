package cn.biq.mn.user.user;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.PasswordField;
import cn.biq.mn.base.validation.UserNameField;

@Getter
@Setter
public class RegisterForm {

    @NotBlank
    @UserNameField
    private String username;

    @NotBlank
    @PasswordField
    private String password;

    @NotBlank
    private String inviteCode;

}
