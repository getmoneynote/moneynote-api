package com.moneynote.currency;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Range;

@Getter @Setter
public class CurrencyCreateForm {

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "{validation.currency.code.pattern}")
    private String code;

    /**
     * Currency.name 列长是 16，与 ValidName 的 64 不同，不能套用组合注解。
     */
    @NotBlank
    @Size(max = 16)
    private String name;

    @NotBlank
    @Size(max = 16)
    private String symbol;

    @NotNull
    @Range(min = 0, max = 4)
    private Integer decimalPlaces;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 8)
    private BigDecimal usdRate;

}
