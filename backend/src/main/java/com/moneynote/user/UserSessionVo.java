package com.moneynote.user;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSessionVo {

    private String username;

    private String photo;

    private Long currentBookId;

    private Long baseCurrencyId;

    private Integer baseCurrencyDecimalPlaces;

    private Long currentLlmConfigId;

}
