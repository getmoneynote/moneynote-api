package com.moneynote.moneyflow;

import com.moneynote.base.IdAndNameDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 账单列表项（只读视图，与 MoneyFlow 实体解耦）。
 *
 * <p>金额字段均为最小货币单位 Long：{@link #amount} 为原币金额（账户币种或账本币种），
 * {@link #convertedAmount} 为换算金额（收支为账本币种、转账为转入账户币种）。
 */
@Getter @Setter
public class MoneyFlowListItem {

    private Long id;

    /** 归属账本（列表展示所属账本用）。 */
    private Long bookId;
    private String bookName;

    private Integer type;

    private String title;

    private String notes;

    /** 原币金额（最小单位）：有账户时与账户币种一致，无账户时按账本币种计量。 */
    private Long amount;

    /** 原币金额的小数位与符号（用于展示 amount）。 */
    private Integer amountCurrencyDecimalPlaces;
    private String amountCurrencySymbol;

    /** 换算金额（最小单位）：收支为账本币种，转账为转入账户币种。 */
    private Long convertedAmount;

    /** 换算金额币种的小数位与符号（即 convertedAmount 的币种，用于展示 ≈）。 */
    private Integer convertedCurrencyDecimalPlaces;
    private String convertedCurrencySymbol;

    /** 原币种与换算金额币种不同时才展示换算值（≈）。 */
    private Boolean needConvert;

    private Long createTime;

    private Boolean confirm;

    private Boolean include;

    private Long accountId;
    private String accountName;

    private Long toAccountId;
    private String toAccountName;

    private Long categoryId;
    private String categoryName;

    private Long payeeId;
    private String payeeName;

    private List<IdAndNameDetails> tags;

}