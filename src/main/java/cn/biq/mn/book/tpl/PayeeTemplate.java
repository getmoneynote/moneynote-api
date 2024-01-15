package cn.biq.mn.book.tpl;


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
    private Integer sort;

}
