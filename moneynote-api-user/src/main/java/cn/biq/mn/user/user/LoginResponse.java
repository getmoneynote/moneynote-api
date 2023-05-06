package cn.biq.mn.user.user;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String username;
    private boolean remember;

}
