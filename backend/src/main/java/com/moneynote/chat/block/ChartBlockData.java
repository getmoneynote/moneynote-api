package com.moneynote.chat.block;

import java.math.BigDecimal;
import java.util.List;

/**
 * 图表块数据：后端定死的最小图表规格，前端只按 chartType 映射到图表组件，不做解释。
 * 数据由生产工具用确定性代码从业务快照映射（不经过模型手写），金额一律为币种主单位。
 *
 * @param chartType    图表形态（pie / column），前端组件映射键
 * @param title        图表标题
 * @param currencyCode 金额币种代码（如 CNY）；整张图同一币种，口径由生产工具保证
 * @param items        数据点（名称 + 主单位金额），已按展示顺序排好
 */
public record ChartBlockData(
        String chartType,
        String title,
        String currencyCode,
        List<Item> items) implements BlockData {

    /** 单个数据点：分类名 + 主单位金额 */
    public record Item(String name, BigDecimal value) {
    }

}
