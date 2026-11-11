package com.moneynote.chat.agent.account;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 账户资产分析快照（只读，供 LLM 工具 getAccountBalances 消费）。
 *
 * <p>金额口径三条硬规则（全 DTO 通用）：
 * <ul>
 *   <li>所有金额字段均为<b>币种主单位</b>的 {@link java.math.BigDecimal}（如人民币「元」），
 *       绝不对外给出最小单位（分）；最小单位只存在于实体与 Service 内部。</li>
 *   <li>{@code baseBalance} 已由 Java 按 USD 锚点折算到本位币，模型直接使用，<b>不得自行乘汇率</b>。</li>
 *   <li>负债类账户（type=2 信用、4 贷款）的余额与 baseBalance 均为<b>负数</b>，
 *       负数金额即负债规模。</li>
 * </ul>
 *
 * <p><b>汇总指标只给三个</b>（totalAssets / totalLiabilities / netWorth），由 Java 逐账户舍入后求和，
 * 与概览页 {@code AccountService.netWorth()} 同口径——模型报"我的净资产多少"时<b>直接用这三个字段</b>，
 * 不要拿明细自己再加一遍（逐账户加总既慢又容易算错，而且会让模型的思考过程变得冗长）。
 * 明细 {@link #accounts} 只用于回答结构类问题（哪几类占比多少、哪个账户最大）。
 *
 * <p>为保证明细求和与汇总一致，{@link #accounts} 已由后端收敛为「只含 include=true 的账户」，
 * 且 {@code baseBalance} 已折算好。
 *
 * <p>不包含：账户卡号 no、canExpense/canIncome/canTransferFrom/canTransferTo 等支付能力标记
 * （模型不需要，且属于多余信息面）。
 */
@Getter @Setter
public class AccountAnalysisResponse {

    /** 本位币 ISO 4217 代码，如 CNY。所有金额字段均以该币种计。 */
    private String baseCurrencyCode;

    /** 总资产（本位币主单位）：typeName 为「活期」「资产」的账户之和。 */
    private BigDecimal totalAssets;

    /**
     * 总负债（本位币主单位）：负债类账户之和取反。
     * 正常为正数；若负债类账户的余额被录成正数（方向录错），此处会变成负数——
     * 不做 abs 或兜底修正，否则净资产将与概览页 {@code netWorth()} 不一致。
     */
    private BigDecimal totalLiabilities;

    /** 净资产 = totalAssets − totalLiabilities，可能为负。 */
    private BigDecimal netWorth;

    /** 计入净资产的账户明细（已排除 include=false 的账户），用于结构分析，不必再求和。 */
    private List<AccountAnalysisItem> accounts;

    /** 该用户全部币种及其汇率，供模型解释币种与折算关系。 */
    private List<AccountAnalysisCurrency> currencies;

}
