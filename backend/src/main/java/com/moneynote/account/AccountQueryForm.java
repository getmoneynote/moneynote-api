package com.moneynote.account;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AccountQueryForm {

    /** 账户类型：1-活期 2-信用 3-资产 4-贷款。不传返回全部类型。 */
    private Integer type;

    /** 名称模糊匹配（忽略大小写）。 */
    private String name;

    /** 币种 id，等值过滤。 */
    private Long currencyId;

    /** 是否计入净资产，等值过滤。 */
    private Boolean include;

    /** 可支出，等值过滤。 */
    private Boolean canExpense;

    /** 可收入，等值过滤。 */
    private Boolean canIncome;

    /** 可转出，等值过滤。 */
    private Boolean canTransferFrom;

    /** 可转入，等值过滤。 */
    private Boolean canTransferTo;

}
