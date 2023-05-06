package cn.biq.mn.user.payee;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameDetails;


@Getter
@Setter
public class PayeeDetails extends IdAndNameDetails {

    private String notes;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;

}
