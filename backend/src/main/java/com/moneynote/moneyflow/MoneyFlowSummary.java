package com.moneynote.moneyflow;

import lombok.Getter;
import lombok.Setter;

/**
 * 账单统计结果（与列表同一套过滤条件，忽略 type）。
 *
 * <p>金额均以用户「本位币」的最小货币单位（Long）计量，由前端用
 * currencySymbol/currencyDecimalPlaces 换算展示，无需二次换算；与账本币种无关。
 */
@Getter @Setter
public class MoneyFlowSummary {

    /** 总支出（本位币最小单位） */
    private Long expense;

    /** 总收入（本位币最小单位） */
    private Long income;

    /** 总结余 = 收入 - 支出（本位币最小单位）。后端算好下发，前端纯展示。 */
    private Long surplus;

    private String currencyCode;

    private String currencySymbol;

    private Integer currencyDecimalPlaces;

}