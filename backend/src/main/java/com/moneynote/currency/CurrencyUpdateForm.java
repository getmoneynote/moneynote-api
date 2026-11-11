package com.moneynote.currency;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CurrencyUpdateForm {

    /** Currency.name 列长是 16 */
    @Size(max = 16)
    private String name;

    @Positive
    @Digits(integer = 12, fraction = 8)
    private BigDecimal usdRate;

}
