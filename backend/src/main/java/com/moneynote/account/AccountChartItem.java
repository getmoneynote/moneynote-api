package com.moneynote.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 图表数据项（本位币口径）。
 *
 * name 为账户名；amount 为本位币「最小货币单位」金额，恒为正数
 * （负债类账户取相反数），百分比由前端图表按占比自行计算。
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountChartItem {

    private String name;

    private Long amount;

}
