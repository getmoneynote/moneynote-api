package com.moneynote.account;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 资产/负债构成分布（本位币口径），用于概览页饼图。
 *
 * assets / liabilities 分别为资产类、负债类账户的本位币金额明细，
 * 只含余额非 0 的 include=true 账户，按金额降序；金额口径与
 * {@link NetWorthSummary} 一致（逐账户换算、先舍入后汇总）。
 */
@Getter @Setter
public class BalanceDistribution {

    private List<AccountChartItem> assets;

    private List<AccountChartItem> liabilities;

    private String currencySymbol;

    private Integer decimalPlaces;

}
