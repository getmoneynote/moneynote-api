package com.moneynote.user;

import com.moneynote.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("error.user.login.credentials.wrong");
    }

}
