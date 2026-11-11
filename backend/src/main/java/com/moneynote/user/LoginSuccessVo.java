package com.moneynote.user;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录成功响应中 {@code data} 字段的结构，携带 {@code accessToken}。
 */
@Getter @Setter
public class LoginSuccessVo {

    private String accessToken;

    public LoginSuccessVo(String accessToken) {
        this.accessToken = accessToken;
    }

}
