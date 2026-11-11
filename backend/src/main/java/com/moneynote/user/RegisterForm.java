package com.moneynote.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterForm {

    @ValidUsername
    private String username;

    @ValidPassword
    private String password;

    @NotBlank
    private String inviteCode;

}
