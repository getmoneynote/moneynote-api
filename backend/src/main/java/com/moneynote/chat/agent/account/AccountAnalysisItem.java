package com.moneynote.chat.agent.account;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * 单个账户的资产分析明细。金额为<b>币种主单位</b>（如人民币「元」），不是最小单位。
 *
 * <p>字段名刻意与实体 {@link com.moneynote.account.Account} 区分：实体里 {@code balance}
 * 是最小单位 Long，这里的 {@code balance} 是主单位 BigDecimal，不要混用。
 */
@Getter @Setter
public class AccountAnalysisItem {

    /** 账户名称。 */
    private String name;

    /** 账户备注，可能为 null。 */
    private String notes;

    /**
     * 账户类型名称：活期 / 信用 / 资产 / 贷款（分别对应 type 1/2/3/4）。
     * 活期与资产为资产类、信用与贷款为负债类；由后端映射，模型直接引用不必自行翻译。
     */
    private String typeName;

    /** 账户自身币种下的当前余额（主单位），负债类账户为<b>负数</b>。 */
    private BigDecimal balance;

    /** 已折算到本位币的余额（主单位），负债为负数；已由 Java 算好，不要自行乘汇率。 */
    private BigDecimal baseBalance;

    /** 信用额度（账户币种主单位），仅信用/贷款账户可能有值，可能为 null。 */
    private BigDecimal creditLimit;

    /** 可用额度（账户币种主单位）= creditLimit + balance；creditLimit 为 null 时也为 null。 */
    private BigDecimal availableCredit;

    /** 账户币种 ISO 4217 代码，如 CNY。 */
    private String currencyCode;

}
