package cn.biq.mn.payee;

import cn.biq.mn.base.IdAndNameDetails;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PayeeDetails extends IdAndNameDetails {

    private String notes;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;
    private Integer sort;

}
