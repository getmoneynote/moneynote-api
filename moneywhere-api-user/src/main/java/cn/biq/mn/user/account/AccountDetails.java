package cn.biq.mn.user.account;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameDetails;
import java.math.BigDecimal;

@Getter @Setter
public class AccountDetails extends IdAndNameDetails {

    private AccountType type;
    private String typeName;
    private String no;
    private BigDecimal balance;
    private BigDecimal convertedBalance;
    private Boolean enable;
    private Boolean include;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransferFrom;
    private Boolean canTransferTo;
    private String notes;
    private String currencyCode;

    private BigDecimal creditLimit;
    private Integer billDay;

    private BigDecimal apr;
    private Long asOfDate;

    public BigDecimal getRemainLimit() {
        if (creditLimit == null) return null;
        return creditLimit.add(getBalance());
    }

}
