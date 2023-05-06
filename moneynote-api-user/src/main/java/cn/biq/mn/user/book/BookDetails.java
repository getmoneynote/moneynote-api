package cn.biq.mn.user.book;

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
    private IdAndNameDetails defaultExpenseAccount;
    private IdAndNameDetails defaultIncomeAccount;
    private IdAndNameDetails defaultTransferFromAccount;
    private IdAndNameDetails defaultTransferToAccount;
    private CategoryDetails defaultExpenseCategory;
    private CategoryDetails defaultIncomeCategory;
    private boolean isDefault;

}
