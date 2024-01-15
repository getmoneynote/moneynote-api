package cn.biq.mn.user;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginForm {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Boolean remember = false; //是否记住7天

}
