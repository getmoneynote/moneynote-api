package com.moneynote.moneyflow;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 流水分类统计（本位币口径），供概览页支出/收入分类环形图使用。
 *
 * categories 为按根分类汇总的金额明细，按金额降序、跳过 0 项；
 * 金额口径与 {@link MoneyFlowSummary} 一致（逐条按原币换算本位币后累加）。
 * 过滤条件与列表/统计共用 {@link MoneyFlowQueryForm}（type 必填且限定 1=支出 / 2=收入）。
 */
@Getter @Setter
public class CategoryStatistics {

    private List<CategoryChartItem> categories;

    private String currencyCode;

    private String currencySymbol;

    private Integer currencyDecimalPlaces;

}
