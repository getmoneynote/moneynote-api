package com.moneynote.moneyflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 分类统计图表数据项（本位币口径）。
 *
 * name 为根分类名（跨账本同名合并）；amount 为本位币「最小货币单位」金额，
 * 百分比由前端图表按占比自行计算。结构与 account 包的 AccountChartItem 一致，
 * 供 CardPie 同一套展示组件复用。
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryChartItem {

    private String name;

    private Long amount;

}
