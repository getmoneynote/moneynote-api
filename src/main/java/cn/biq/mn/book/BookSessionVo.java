package cn.biq.mn.book;

import cn.biq.mn.base.IdAndNameDetails;
import cn.biq.mn.account.AccountDetails;
import cn.biq.mn.category.CategoryDetails;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookSessionVo extends IdAndNameDetails {

    private String defaultCurrencyCode;

    private AccountDetails defaultExpenseAccount;

    private AccountDetails defaultIncomeAccount;

    private AccountDetails defaultTransferFromAccount;

    private AccountDetails defaultTransferToAccount;

    private CategoryDetails defaultExpenseCategory;

    private CategoryDetails defaultIncomeCategory;

}
