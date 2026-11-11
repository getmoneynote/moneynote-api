package com.moneynote.payee;

import com.moneynote.validate.ValidNotes;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayeeUpdateForm {

    /** name 可空：支持只更新某个布尔的局部 PATCH（列表 Switch 就地切换），与 TagUpdateForm 一致 */
    @Size(max = 64)
    private String name;

    @ValidNotes
    private String notes;

    private Boolean canExpense;

    private Boolean canIncome;

}
