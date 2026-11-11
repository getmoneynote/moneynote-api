package com.moneynote.account;

import lombok.Getter;
import lombok.Setter;

/**
 * 净资产概览汇总（本位币口径）。
 *
 * totalAssets / totalLiabilities / netWorth 均为本位币「最小货币单位」（如分），
 * 与 {@link AccountBalanceSummary} 口径一致；totalLiabilities 恒为正数（负债规模），
 * netWorth = totalAssets - totalLiabilities，可能为负。
 */
@Getter @Setter
public class NetWorthSummary {

    private Long totalAssets;
    private Long totalLiabilities;
    private Long netWorth;
    private String currencySymbol;
    private Integer decimalPlaces;

}
