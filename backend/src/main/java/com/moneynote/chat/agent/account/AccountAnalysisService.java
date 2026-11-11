package com.moneynote.chat.agent.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.account.Account;
import com.moneynote.account.AccountRepository;
import com.moneynote.currency.Currency;
import com.moneynote.currency.CurrencyRepository;
import com.moneynote.currency.CurrencyService;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.user.User;
import com.moneynote.user.UserRepository;

/**
 * 账户资产分析快照服务：只读、显式 userId。
 *
 * <p>为什么不复用 {@code AccountService}：它的 {@code netWorth()} / {@code balanceDistribution()} /
 * {@code query()} 全部依赖 {@code CurrentUserHolder} / {@code SessionManager}（ThreadLocal），
 * 而本服务会被 LLM 工具在响应式线程上调用，那里没有请求上下文。因此这里所有方法都显式接收
 * {@code userId}，不碰任何 ThreadLocal。
 *
 * <p>职责边界：本服务做<b>取数 + 口径收敛 + 逐账户折算 + 三个汇总数字</b>。
 * 总资产/总负债/净资产由 Java 在最小单位上累加后一次换算（与 {@code AccountService.netWorth()}
 * 同口径，逐账户舍入后求和），模型直接引用即可，不需要拿明细再加总——这既保证数字与概览页一致，
 * 也避免推理模型把整段上下文耗在逐账户心算上。
 * 口径收敛：只返回 include=true 的账户；负债类账户（type 2 信用 / 4 贷款）的 baseBalance 为负数，
 * 汇总时取相反数得到正数负债规模。其余指标（资产负债率、结构占比、额度合计）由模型基于明细推导。
 */
@Service
@RequiredArgsConstructor
public class AccountAnalysisService {

    /** 资产类账户：type 1-活期、3-资产，余额为正 */
    private static final Set<Integer> ASSET_TYPES = Set.of(1, 3);

    /** 负债类账户：type 2-信用、4-贷款，余额为负 */
    private static final Set<Integer> DEBT_TYPES = Set.of(2, 4);

    /** 账户类型名称；键必须覆盖 Account.type 的全部合法值（1–4） */
    private static final Map<Integer, String> TYPE_NAMES = Map.of(
            1, "活期",
            2, "信用",
            3, "资产",
            4, "贷款");

    /** 资产类账户类型名：从 {@link #ASSET_TYPES} 派生，供图表工具按名筛选（DTO 只暴露 typeName）。 */
    public static final Set<String> ASSET_TYPE_NAMES = ASSET_TYPES.stream()
            .map(TYPE_NAMES::get)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private static final String TYPE_UNKNOWN = "未知类型";

    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;
    private final UserRepository userRepository;

    /**
     * 生成当前用户的账户分析快照（只含计入净资产的账户）。只读，不修改任何实体。
     *
     * @param userId 用户 id（由调用方在请求线程上取好，禁止在此读 ThreadLocal）
     * @throws ResourceInvalidException 用户不存在、本位币未设置或悬空、账户币种缺失、
     *                                  汇率非正数——均属数据契约破坏，不能静默按 0 展示
     */
    @Transactional(readOnly = true)
    public AccountAnalysisResponse snapshot(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));

        Map<Long, Currency> currencyMap = loadCurrencyMap(userId);
        Currency baseCurrency = requireBaseCurrency(user.getBaseCurrencyId(), currencyMap);
        int baseDecimalPlaces = baseCurrency.getDecimalPlaces();

        // 第一遍：过滤口径、逐账户折算并舍入到本位币最小单位（与 AccountService.netWorth() 同路径）
        List<Account> includedAccounts = new ArrayList<>();
        Map<Long, Long> baseMinorByAccountId = new HashMap<>();
        Map<Long, Currency> accountCurrencyByAccountId = new HashMap<>();
        // 汇总在最小单位上累加，最后一次性换算，避免中间精度漂移
        long totalAssetsMinor = 0;
        long totalDebtMinor = 0;
        for (Account account : accountRepository.findByUserId(userId)) {
            // 口径收敛：只返回计入净资产的账户。include=false 的账户不进列表，
            // 这样模型「看明细」与「看汇总」看到的是同一批账户，口径不会打架
            if (!Boolean.TRUE.equals(account.getInclude())) {
                continue;
            }
            Currency accountCurrency = currencyMap.get(account.getCurrency().getId());
            if (accountCurrency == null) {
                // 账户币种悬空：无法折算，属于数据契约破坏，不能跳过（跳过会让汇总悄悄少算）
                throw new ResourceInvalidException("error.currency.base.missing");
            }
            long baseMinor = currencyService.convert(account.getBalance(), accountCurrency, baseCurrency);

            includedAccounts.add(account);
            accountCurrencyByAccountId.put(account.getId(), accountCurrency);
            baseMinorByAccountId.put(account.getId(), baseMinor);

            if (ASSET_TYPES.contains(account.getType())) {
                totalAssetsMinor += baseMinor;
            } else if (DEBT_TYPES.contains(account.getType())) {
                totalDebtMinor += baseMinor; // 负债账户余额为负，此处累加后取反即负债规模
            }
        }

        // 排序：type 升序 → 本位币余额降序 → name 升序（name 对 user 唯一，保证顺序稳定不漂移）；
        // 同类型聚拢、大额在前，读起来更接近人工汇报顺序
        includedAccounts.sort(Comparator
                .comparing(Account::getType)
                .thenComparing(account -> baseMinorByAccountId.get(account.getId()), Comparator.reverseOrder())
                .thenComparing(Account::getName));

        // 第二遍：映射为对外 DTO（金额一律转主单位）
        List<AccountAnalysisItem> items = new ArrayList<>(includedAccounts.size());
        for (Account account : includedAccounts) {
            Currency accountCurrency = accountCurrencyByAccountId.get(account.getId());
            int decimals = accountCurrency.getDecimalPlaces();

            AccountAnalysisItem item = new AccountAnalysisItem();
            item.setName(account.getName());
            item.setNotes(account.getNotes());
            item.setTypeName(typeName(account.getType()));
            item.setBalance(toMajor(account.getBalance(), decimals));
            item.setBaseBalance(toMajor(baseMinorByAccountId.get(account.getId()), baseDecimalPlaces));
            item.setCreditLimit(account.getCreditLimit() == null
                    ? null : toMajor(account.getCreditLimit(), decimals));
            item.setAvailableCredit(account.getCreditLimit() == null
                    ? null : toMajor(account.getCreditLimit() + account.getBalance(), decimals));
            item.setCurrencyCode(accountCurrency.getCode());
            items.add(item);
        }

        AccountAnalysisResponse response = new AccountAnalysisResponse();
        response.setBaseCurrencyCode(baseCurrency.getCode());
        response.setTotalAssets(toMajor(totalAssetsMinor, baseDecimalPlaces));
        // 负债对外报正数（负债规模），内部累加值是负数
        response.setTotalLiabilities(toMajor(-totalDebtMinor, baseDecimalPlaces));
        response.setNetWorth(toMajor(totalAssetsMinor + totalDebtMinor, baseDecimalPlaces));
        response.setAccounts(items);
        response.setCurrencies(buildCurrencyList(currencyMap));
        return response;
    }

    /** 一次性加载当前用户所有币种并按 id 建索引（账户与账户币种各只查一次库，避免 N+1）。 */
    private Map<Long, Currency> loadCurrencyMap(Long userId) {
        Map<Long, Currency> currencyMap = new HashMap<>();
        for (Currency currency : currencyRepository.findByUserId(userId)) {
            // 汇率非正数无法参与折算（除零/反向），属数据契约破坏，不能静默跳过
            if (currency.getUsdRate() == null || currency.getUsdRate().signum() <= 0) {
                throw new ResourceInvalidException("error.currency.base.missing");
            }
            currencyMap.put(currency.getId(), currency);
        }
        return currencyMap;
    }

    /**
     * 解析本位币：user.baseCurrencyId 是冗余 Long（无外键），未设置或记录被删都会悬空。
     * 两种情况都是数据契约破坏，统一抛 error.currency.base.missing；根因已在
     * CurrencyService.delete 堵住（禁止删除当前本位币），此处为纵深防御。
     */
    private Currency requireBaseCurrency(Long baseCurrencyId, Map<Long, Currency> currencyMap) {
        if (baseCurrencyId == null) {
            throw new ResourceInvalidException("error.currency.base.missing");
        }
        Currency baseCurrency = currencyMap.get(baseCurrencyId);
        if (baseCurrency == null) {
            throw new ResourceInvalidException("error.currency.base.missing");
        }
        return baseCurrency;
    }

    /** 币种清单按 code 升序（跨数据库稳定），供模型解释币种与折算关系。 */
    private List<AccountAnalysisCurrency> buildCurrencyList(Map<Long, Currency> currencyMap) {
        return currencyMap.values().stream()
                .sorted(Comparator.comparing(Currency::getCode))
                .map(currency -> {
                    AccountAnalysisCurrency item = new AccountAnalysisCurrency();
                    item.setCode(currency.getCode());
                    item.setName(currency.getName());
                    item.setSymbol(currency.getSymbol());
                    item.setDecimalPlaces(currency.getDecimalPlaces());
                    item.setUsdRate(currency.getUsdRate());
                    return item;
                })
                .toList();
    }

    /**
     * 最小货币单位（long）→ 币种主单位（BigDecimal），按该币种小数位显式定标，
     * 保证输出形如 10.50 而不是 10.5 / 10.50000000。
     */
    private static BigDecimal toMajor(long minor, int decimalPlaces) {
        return BigDecimal.valueOf(minor)
                .movePointLeft(decimalPlaces)
                .setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    private static String typeName(Integer type) {
        // type 在库里非空且限定 1–4，这里仍防一手 null：TYPE_NAMES 是 Map.of（不可变），不接受 null 键
        return type == null ? TYPE_UNKNOWN : TYPE_NAMES.getOrDefault(type, TYPE_UNKNOWN);
    }

}
