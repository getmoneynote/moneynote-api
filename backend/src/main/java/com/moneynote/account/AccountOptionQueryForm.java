package com.moneynote.account;

import lombok.Getter;
import lombok.Setter;

/**
 * /accounts/options 查询参数。与 AccountQueryForm 分开定义：list 是管理视角，
 * options 是「可选视角」，用途开关过滤只服务于下拉数据源场景，不混入 list 契约。
 * canXxx 语义与旧版 AccountQueryForm 一致：传值即按等值过滤，不传不限制。
 */
@Getter @Setter
public class AccountOptionQueryForm {

    /**
     * 账户类型：1-活期 2-信用 3-资产 4-贷款。
     * 不传返回全部类型；超出范围与 query() 一致：不报错，返回空列表。
     */
    private Integer type;

    private Boolean canExpense;

    private Boolean canIncome;

    private Boolean canTransferFrom;

    private Boolean canTransferTo;

}
