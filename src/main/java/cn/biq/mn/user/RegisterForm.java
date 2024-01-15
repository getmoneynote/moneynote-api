package cn.biq.mn.user;


import cn.biq.mn.validation.PasswordField;
import cn.biq.mn.validation.UserNameField;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

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
