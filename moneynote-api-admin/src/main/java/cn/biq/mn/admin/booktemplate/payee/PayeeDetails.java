package cn.biq.mn.admin.booktemplate.payee;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;


@Getter @Setter
public class PayeeDetails extends BaseDetails {

    private String name;
    private String notes;
    private Boolean canExpense;
    private Boolean canIncome;

}
