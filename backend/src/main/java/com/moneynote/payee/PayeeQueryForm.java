package com.moneynote.payee;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PayeeQueryForm {

    /**
     * 可选：指定账本 id。不传 = 当前账本；传了必须属于当前用户，否则 400。
     * 供账单搜索场景按所选账本拉取交易对象选项（与 /categories/tree 的 bookId 语义一致）。
     */
    private Long bookId;

    /**
     * 二者均为可选的过滤条件；不传 = 不过滤该维度（"全部"）。
     * 二者可任意组合，互不互斥——与实体中两个布尔开关的语义一致。
     */
    private Boolean canExpense;

    private Boolean canIncome;

}