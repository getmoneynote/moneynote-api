package com.moneynote.moneyflow;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * 账单查询表单，所有字段均为可选，组合成 AND 关系的过滤条件。
 *
 * <p>本表单同时供列表（list）与统计（statistics）两个端点复用；
 * 统计会忽略 {@link #type}（永远按支出/收入两类汇总），其余条件与列表完全一致。
 */
@Getter @Setter
public class MoneyFlowQueryForm {

    /**
     * 账本过滤：为空 = 当前用户全部账本；有值 = 只查该账本。
     */
    private Long bookId;

    /**
     * 流水类型：1=支出 2=收入 3=转账 4=余额调整。
     * 列表过滤用；statistics 忽略本字段。
     */
    private Integer type;

    private String title;

    private String notes;

    /**
     * 金额下界（账本币种主单位，如 10.5 元）。
     * 后端按账本币种小数位换算成最小单位后，过滤 convertedAmount。
     */
    private BigDecimal minAmount;

    /** 金额上界（账本币种主单位）。 */
    private BigDecimal maxAmount;

    /** 发生时间区间（Unix 毫秒），闭区间。 */
    private Long minTime;
    private Long maxTime;

    /** 账户过滤：匹配「转出账户」或「转入账户」任一（含对方账户）。 */
    private Long accountId;

    /** 仅匹配转入账户（转账）。 */
    private Long toAccountId;

    /** 分类 id（逗号分隔；前端已把选中的父分类展开为叶子 id 后传值）。 */
    private String categoryIds;

    /** 交易对象 id（逗号分隔）。 */
    private String payeeIds;

    /** 标签 id（逗号分隔；标签为扁平结构，无需展开）。 */
    private String tagIds;

    private Boolean confirm;

    private Boolean include;

}