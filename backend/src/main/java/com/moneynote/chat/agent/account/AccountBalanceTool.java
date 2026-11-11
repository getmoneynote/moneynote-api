package com.moneynote.chat.agent.account;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 账户余额查询工具：把 {@link AccountAnalysisService#snapshot(Long)} 暴露给 LLM。
 *
 * <p>为什么不是 Spring Bean：工具实例必须绑定「本次执行的用户」，做成单例会被并发请求串用；
 * 因此每次执行由 Agent 用 {@code new} 创建，只持有不可变的 {@code userId}。
 *
 * <p>线程注意：本方法在流管线线程上被调用（非请求线程），所以身份来自构造参数而非
 * ThreadLocal；{@link AccountAnalysisService} 已按此前提写成显式 userId 形式。
 *
 * <p>只注册读方法：项目约束是「写库方法绝不注册给 LLM」（否则模型一次幻觉就能改账）。
 */
public class AccountBalanceTool {

    private final AccountAnalysisService accountAnalysisService;
    private final Long userId;

    public AccountBalanceTool(AccountAnalysisService accountAnalysisService, Long userId) {
        this.accountAnalysisService = accountAnalysisService;
        this.userId = userId;
    }

    @Tool(name = "getAccountBalances", description = """
            获取当前用户「计入净资产」的账户快照。回答资产、负债、净资产、资产结构类问题时先调用本工具。
            返回：baseCurrencyCode 本位币代码、totalAssets 总资产、totalLiabilities 总负债（正数）、
            netWorth 净资产、accounts 账户明细、currencies 币种与汇率。
            accounts 每项：name 账户名、notes 备注（可空）、typeName 账户类型名、balance 账户币种下的余额、
            baseBalance 折算到本位币的余额、creditLimit 信用额度（可空）、availableCredit 可用额度（可空）、
            currencyCode 账户币种。
            口径（必须遵守）：
            1) 所有金额都是「币种主单位」（元/美元），不是分；
            2) 总资产、总负债、净资产直接引用返回的三个汇总字段，不要拿明细重新加总——重复加总既容易算错，
               也会让回答变慢；
            3) baseBalance 已由系统按汇率折算到本位币，不要自己乘汇率，也不要把 balance 跨币种相加；
            4) typeName 只有四种：活期 / 信用 / 资产 / 贷款。活期与资产属资产类，信用与贷款属负债类；
               负债类账户的金额是负数，报负债规模时用 totalLiabilities（已是正数），不要取明细里的负数相加；
               仅当需要按类别拆结构时才用明细自己汇总某一类的 baseBalance；
            5) 列表已排除「不计入净资产」的账户，无需再筛选；
            6) 只返回当前余额，没有历史数据，也没有收入、支出、月供、收益字段。
            """)
    public AccountAnalysisResponse getAccountBalances() {
        return accountAnalysisService.snapshot(userId);
    }

}
