package cn.biq.mn.user.book;

import cn.biq.mn.user.account.AccountDetails;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameDetails;
import cn.biq.mn.user.category.CategoryDetails;

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

}
