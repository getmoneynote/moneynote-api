package com.moneynote.user;

public class UserConverter {

    public static UserSessionVo toVo(User user){
        UserSessionVo vo = new UserSessionVo();
        vo.setUsername(user.getUsername());
        vo.setPhoto(user.getPhoto());
        vo.setCurrentBookId(user.getCurrentBookId());
        vo.setBaseCurrencyId(user.getBaseCurrencyId());
        vo.setCurrentLlmConfigId(user.getCurrentLlmConfigId());
        return vo;
    }

}
