package cn.biq.mn.book;

import cn.biq.mn.base.IdAndNameDetails;
import cn.biq.mn.account.AccountDetails;
import cn.biq.mn.category.CategoryDetails;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDetails extends IdAndNameDetails {

    private IdAndNameDetails group;
    private String notes;
    private Boolean enable;
    private String defaultCurrencyCode;
    private AccountDetails defaultExpenseAccount;
    private AccountDetails defaultIncomeAccount;
    private AccountDetails defaultTransferFromAccount;
    private AccountDetails defaultTransferToAccount;
    private CategoryDetails defaultExpenseCategory;
    private CategoryDetails defaultIncomeCategory;
    private boolean isDefault;
    private Integer sort;

}
