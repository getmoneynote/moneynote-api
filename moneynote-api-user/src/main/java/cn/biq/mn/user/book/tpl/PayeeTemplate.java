package cn.biq.mn.user.book.tpl;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayeeTemplate {

    private Integer id;
    private String name;
    private String notes;
    private Boolean canExpense;
    private Boolean canIncome;

}
