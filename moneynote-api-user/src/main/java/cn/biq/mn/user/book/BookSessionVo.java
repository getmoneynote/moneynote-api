package cn.biq.mn.user.book;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameDetails;
import cn.biq.mn.user.account.AccountDetails;
import cn.biq.mn.user.category.CategoryDetails;

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
