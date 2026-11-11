package com.moneynote.chat.agent.account;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * 币种及其汇率信息（供模型解释多币种折算关系）。
 *
 * <p>这里只做说明用途：模型<b>不应</b>用 code + usdRate 自己算换算值，
 * 账户级折算结果已由 Java 在 {@link AccountAnalysisItem#getBaseBalance()} 给出。
 */
@Getter @Setter
public class AccountAnalysisCurrency {

    /** ISO 4217 三位代码，如 CNY。 */
    private String code;

    /** 币种名称，如「人民币」。 */
    private String name;

    /** 币种符号，如 ¥。 */
    private String symbol;

    /** 最小货币单位的小数位（CNY 2、USD 2、VND 0），仅说明精度。 */
    private Integer decimalPlaces;

    /** 汇率：1 USD 可兑换该币种的数量（USD 自身为 1）。 */
    private BigDecimal usdRate;

}
