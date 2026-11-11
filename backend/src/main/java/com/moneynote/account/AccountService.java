package com.moneynote.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.book.Book;
import com.moneynote.book.BookService;
import com.moneynote.currency.Currency;
import com.moneynote.currency.CurrencyRepository;
import com.moneynote.currency.CurrencyService;
import com.moneynote.exception.ResourceDuplicateException;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.moneyflow.MoneyFlow;
import com.moneynote.moneyflow.MoneyFlowRepository;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.SessionManager;
import com.moneynote.user.UserRepository;

import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int SCALE = 8;

    private static final int MIN_TYPE = 1;
    private static final int MAX_TYPE = 4;

    /** 余额调整流水的 type（MoneyFlow.type），仅本类 adjustBalance 写入历史记录，不走 MoneyFlowService.create。 */
    private static final int TYPE_ADJUST = 4;

    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;
    private final BookService bookService;
    private final MoneyFlowRepository moneyFlowRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(AccountCreateForm form) {
        if (form.getType() < MIN_TYPE || form.getType() > MAX_TYPE) {
            throw new ResourceInvalidException("error.invalid.argument");
        }
        Long userId = CurrentUserHolder.get();
        if (accountRepository.existsByUserIdAndName(userId, form.getName())) {
            throw new ResourceDuplicateException("resource.account", form.getName());
        }
        Currency currency = currencyRepository.findByIdAndUserId(form.getCurrencyId(), userId)
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
        Account entity = new Account();
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setName(form.getName());
        entity.setType(form.getType());
        entity.setCurrency(currency);
        entity.setBalance(form.getBalance());
        entity.setInclude(form.getInclude());
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        entity.setCanTransferFrom(form.getCanTransferFrom());
        entity.setCanTransferTo(form.getCanTransferTo());
        entity.setNo(form.getNo());
        entity.setCreditLimit(form.getCreditLimit());
        entity.setBillDay(form.getBillDay());
        entity.setApr(form.getApr());
        entity.setDisplayOrder(form.getDisplayOrder());
        entity.setNotes(form.getNotes());
        accountRepository.save(entity);
    }

    @Transactional
    public void update(Long id, AccountUpdateForm form) {
        Long userId = CurrentUserHolder.get();
        Account entity = accountRepository.findByIdAndUserId(id, userId).orElse(null);
        if (entity == null) {
            throw new ResourceNotFoundException("resource.account");
        }
        if (form.getName() != null && !form.getName().equals(entity.getName())) {
            if (accountRepository.existsByUserIdAndName(userId, form.getName())) {
                throw new ResourceDuplicateException("resource.account", form.getName());
            }
            entity.setName(form.getName());
        }
        if (form.getInclude() != null) {
            entity.setInclude(form.getInclude());
        }
        if (form.getCanExpense() != null) {
            entity.setCanExpense(form.getCanExpense());
        }
        if (form.getCanIncome() != null) {
            entity.setCanIncome(form.getCanIncome());
        }
        if (form.getCanTransferFrom() != null) {
            entity.setCanTransferFrom(form.getCanTransferFrom());
        }
        if (form.getCanTransferTo() != null) {
            entity.setCanTransferTo(form.getCanTransferTo());
        }
        if (form.getNo() != null) {
            entity.setNo(form.getNo());
        }
        if (form.getCreditLimit() != null) {
            entity.setCreditLimit(form.getCreditLimit());
        }
        if (form.getBillDay() != null) {
            entity.setBillDay(form.getBillDay());
        }
        if (form.getApr() != null) {
            entity.setApr(form.getApr());
        }
        if (form.getDisplayOrder() != null) {
            entity.setDisplayOrder(form.getDisplayOrder());
        }
        if (form.getNotes() != null) {
            entity.setNotes(form.getNotes());
        }
    }

    @Transactional
    public void delete(Long id) {
        Long userId = CurrentUserHolder.get();
        accountRepository.findByIdAndUserId(id, userId).ifPresent(accountRepository::delete);
    }

    /**
     * 余额调整：把账户余额覆盖为目标值（目标余额覆盖语义）。差额 = 目标 − 当前，可为负。
     * 差额为 0 视为无意义调整；调整立即生效（confirmed 恒 true），并写一条 type=4（ADJUST）的
     * MoneyFlow 历史记录供追溯（旧版 AccountService.adjustBalance 同此设计，非 MoneyFlowService.create）。
     */
    @Transactional
    public void adjustBalance(Long id, AdjustBalanceAddForm form) {
        Long userId = CurrentUserHolder.get();
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.account"));

        long delta = form.getBalance() - account.getBalance();
        if (delta == 0) {
            throw new ResourceInvalidException("error.account.adjust.balance.same");
        }
        account.setBalance(form.getBalance());
        accountRepository.save(account);

        Book book = bookService.getCurrentBook();
        MoneyFlow flow = new MoneyFlow();
        flow.setBook(book);
        flow.setType(TYPE_ADJUST);
        flow.setAmount(delta);
        flow.setConvertedAmount(currencyService.convert(delta, account.getCurrency(), book.getCurrency()));
        flow.setAccount(account);
        flow.setCreateTime(form.getCreateTime());
        flow.setTitle(form.getTitle());
        flow.setNotes(form.getNotes());
        flow.setConfirm(true);
        flow.setInclude(form.getInclude());
        moneyFlowRepository.save(flow);
    }

    @Transactional(readOnly = true)
    public Page<AccountListItem> query(AccountQueryForm form, Pageable page) {
        Long userId = CurrentUserHolder.get();
        Long baseCurrencyId = SessionManager.getCurrentUser().getBaseCurrencyId();

        // 一次性加载所有币种，避免 N+1
        List<Currency> allCurrencies = currencyRepository.findByUserId(userId);
        Map<Long, Currency> currencyMap = new HashMap<>();
        for (Currency c : allCurrencies) {
            currencyMap.put(c.getId(), c);
        }

        // baseCurrencyId 正常不会为 null（注册 initData 必设）；get(null) 对 HashMap 也安全。
        // baseCurrency 仍可能为 null：baseCurrencyId 是无 FK 的冗余 Long，本位币被删后会悬空。
        Currency baseCurrency = currencyMap.get(baseCurrencyId);
        BigDecimal baseUsdRate = baseCurrency.getUsdRate();
        int baseDecimalPlaces = baseCurrency.getDecimalPlaces();
        BigDecimal baseMultiplier = BigDecimal.TEN.pow(baseDecimalPlaces);

        Page<Account> entityPage = accountRepository.findAll(buildSpec(userId, form), page);
        return entityPage.map(account -> {
            AccountListItem item = new AccountListItem();
            item.setId(account.getId());
            item.setName(account.getName());
            item.setType(account.getType());
            item.setNotes(account.getNotes());
            item.setNo(account.getNo());
            item.setBalance(account.getBalance());
            item.setInclude(account.getInclude());
            item.setCanExpense(account.getCanExpense());
            item.setCanIncome(account.getCanIncome());
            item.setCanTransferFrom(account.getCanTransferFrom());
            item.setCanTransferTo(account.getCanTransferTo());
            item.setCreditLimit(account.getCreditLimit());
            // 剩余额度 = 信用额度 + 余额（信用/贷款账户余额为负）；未设额度时为 null
            item.setRemainLimit(account.getCreditLimit() != null
                    ? account.getCreditLimit() + account.getBalance()
                    : null);
            item.setBillDay(account.getBillDay());
            item.setApr(account.getApr());
            item.setDisplayOrder(account.getDisplayOrder());

            Currency accountCurrency = currencyMap.get(account.getCurrency().getId());
            item.setCurrencyId(accountCurrency.getId());
            item.setCurrencyName(accountCurrency.getName());
            item.setCurrencySymbol(accountCurrency.getSymbol());
            item.setCurrencyDecimalPlaces(accountCurrency.getDecimalPlaces());

            // 是否本位币由后端判定并显式下发，前端据实展示换算值（勿让前端用 balance==baseBalance 反推）
            boolean isBaseCurrency = accountCurrency.getId().equals(baseCurrencyId);
            item.setIsBaseCurrency(isBaseCurrency);

            // 换算本位币余额（逐账户舍入到最小单位，balanceSummary() 求和与此口径一致）
            item.setBaseCurrencySymbol(baseCurrency.getSymbol());
            item.setBaseCurrencyDecimalPlaces(baseDecimalPlaces);
            if (isBaseCurrency) {
                item.setBaseBalance(account.getBalance());
            } else {
                item.setBaseBalance(convertToBaseCurrency(account.getBalance(), accountCurrency, baseUsdRate, baseMultiplier));
            }

            return item;
        });
    }

    /**
     * 列表与余额汇总共用的查询条件：全部可选，AND 组合。
     * 保证 balanceSummary() 统计的永远是 query() 列表查出来的同一批账户。
     */
    private Specification<Account> buildSpec(Long userId, AccountQueryForm form) {
        return (root, query, cb) -> {
            Predicate p = cb.equal(root.get("user").get("id"), userId);
            if (form.getType() != null) {
                p = cb.and(p, cb.equal(root.get("type"), form.getType()));
            }
            if (form.getName() != null && !form.getName().isBlank()) {
                String kw = "%" + form.getName().trim().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.<String>get("name")), kw));
            }
            if (form.getCurrencyId() != null) {
                p = cb.and(p, cb.equal(root.get("currency").get("id"), form.getCurrencyId()));
            }
            if (form.getInclude() != null) {
                p = cb.and(p, cb.equal(root.get("include"), form.getInclude()));
            }
            if (form.getCanExpense() != null) {
                p = cb.and(p, cb.equal(root.get("canExpense"), form.getCanExpense()));
            }
            if (form.getCanIncome() != null) {
                p = cb.and(p, cb.equal(root.get("canIncome"), form.getCanIncome()));
            }
            if (form.getCanTransferFrom() != null) {
                p = cb.and(p, cb.equal(root.get("canTransferFrom"), form.getCanTransferFrom()));
            }
            if (form.getCanTransferTo() != null) {
                p = cb.and(p, cb.equal(root.get("canTransferTo"), form.getCanTransferTo()));
            }
            return p;
        };
    }

    /**
     * 账户下拉选项：轻量、不分页，供前端各表单下拉专用（区别于管理页的 query()）。
     * 单用户账户量级小，查出全量后在内存统一过滤（type 与 canXxx 同一处收敛），
     * 不为 options 单独增加 repository 查询方法。
     * canXxx 按等值过滤，规则收敛在后端，前端不再拉全量自行过滤。
     */
    @Transactional(readOnly = true)
    public List<AccountOption> listOptions(AccountOptionQueryForm form) {
        Long userId = CurrentUserHolder.get();

        // 一次性加载所有币种，避免 N+1（与 query() 同策略）
        Map<Long, Currency> currencyMap = new HashMap<>();
        for (Currency c : currencyRepository.findByUserId(userId)) {
            currencyMap.put(c.getId(), c);
        }

        return accountRepository.findByUserId(userId).stream()
                .filter(a -> form.getType() == null || form.getType().equals(a.getType()))
                .filter(a -> form.getCanExpense() == null || form.getCanExpense().equals(a.getCanExpense()))
                .filter(a -> form.getCanIncome() == null || form.getCanIncome().equals(a.getCanIncome()))
                .filter(a -> form.getCanTransferFrom() == null || form.getCanTransferFrom().equals(a.getCanTransferFrom()))
                .filter(a -> form.getCanTransferTo() == null || form.getCanTransferTo().equals(a.getCanTransferTo()))
                // 排序必须稳定：先按 displayOrder 升序（null 视为最大，排在已设置的后面），
                // 同序值内再按 name 升序——name 对 user 唯一（t_account unique(user_id, name)），
                // 保证顺序稳定不漂移。与 list 接口默认排序（displayOrder nulls last, balance desc）呼应
                .sorted(Comparator
                        .comparing(Account::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Account::getName))
                .map(account -> {
                    AccountOption option = new AccountOption();
                    option.setValue(account.getId());
                    option.setLabel(account.getName());
                    option.setType(account.getType());
                    Currency currency = currencyMap.get(account.getCurrency().getId());
                    option.setCurrencyId(currency.getId());
                    option.setCurrencySymbol(currency.getSymbol());
                    option.setCurrencyDecimalPlaces(currency.getDecimalPlaces());
                    return option;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountBalanceSummary balanceSummary(AccountQueryForm form) {
        Long userId = CurrentUserHolder.get();
        Long baseCurrencyId = SessionManager.getCurrentUser().getBaseCurrencyId();
        // 与 query() 共用 buildSpec：汇总的账户集合与列表一致
        List<Account> accountList = accountRepository.findAll(buildSpec(userId, form));

        AccountBalanceSummary summary = new AccountBalanceSummary();

        if (baseCurrencyId == null || accountList.isEmpty()) {
            if (baseCurrencyId == null) {
                // 注册 initData 必设本位币，null 属于数据契约破坏，不能静默按 0 展示
                throw new ResourceInvalidException("error.currency.base.missing");
            }
            summary.setTotal(0L);
            summary.setDecimalPlaces(2);
            return summary;
        }

        // 一次性加载所有币种（含本位币，直接从 map 取，不单独查库）
        List<Currency> allCurrencies = currencyRepository.findByUserId(userId);
        Map<Long, Currency> currencyMap = new HashMap<>();
        for (Currency c : allCurrencies) {
            currencyMap.put(c.getId(), c);
        }
        Currency baseCurrency = currencyMap.get(baseCurrencyId);
        if (baseCurrency == null) {
            // 本位币 id 悬空（币种记录缺失）属于数据契约破坏，不能静默按 0 展示；
            // 根因已在 CurrencyService.delete 里堵住（禁止删除当前本位币），此处为纵深防御
            throw new ResourceInvalidException("error.currency.base.missing");
        }

        BigDecimal baseUsdRate = baseCurrency.getUsdRate();
        int baseDecimalPlaces = baseCurrency.getDecimalPlaces();
        BigDecimal baseMultiplier = BigDecimal.TEN.pow(baseDecimalPlaces);

        // 舍入口径与 query() 一致：逐账户先舍入到本位币最小单位再求和，
        // 保证"列表各行本位币值之和 == 汇总条 total"（query() 处有互指注释）
        long total = 0;
        long totalCreditLimit = 0;
        for (Account account : accountList) {
            Currency accountCurrency = currencyMap.get(account.getCurrency().getId());
            if (accountCurrency == null) {
                continue;
            }
            total += convertToBaseCurrency(account.getBalance(), accountCurrency, baseUsdRate, baseMultiplier);
            if (account.getCreditLimit() != null) {
                totalCreditLimit += convertToBaseCurrency(
                        account.getCreditLimit(), accountCurrency, baseUsdRate, baseMultiplier);
            }
        }

        summary.setTotal(total);
        summary.setTotalCreditLimit(totalCreditLimit);
        summary.setTotalAvailable(totalCreditLimit + total);
        summary.setCurrencySymbol(baseCurrency.getSymbol());
        summary.setDecimalPlaces(baseDecimalPlaces);
        return summary;
    }

    /** 资产类账户：type 1-活期、3-资产 */
    private static final Set<Integer> ASSET_TYPES = Set.of(1, 3);

    /** 负债类账户：type 2-信用、4-贷款（余额为负数，负数金额即负债规模） */
    private static final Set<Integer> DEBT_TYPES = Set.of(2, 4);

    /**
     * 净资产概览：总资产 / 总负债 / 净资产（本位币最小单位）。
     *
     * 只统计 include=true（计入净资产）的账户——t_account.include 字段即为此语义存在，
     * 用户把某个账户排除在净资产外时应立即反映到概览。
     *
     * 计算口径与 {@link #balanceSummary} 严格一致：逐账户按 USD 锚点换算到本位币，
     * 先舍入到最小单位再求和；负债类账户（信用/贷款）余额为负，负债规模取其相反数。
     * netWorth = totalAssets - totalLiabilities。
     */
    @Transactional(readOnly = true)
    public NetWorthSummary netWorth() {
        Long userId = CurrentUserHolder.get();
        Map<Long, Currency> currencyMap = loadCurrencyMap(userId);
        Currency baseCurrency = requireBaseCurrency(currencyMap);

        BigDecimal baseUsdRate = baseCurrency.getUsdRate();
        BigDecimal baseMultiplier = BigDecimal.TEN.pow(baseCurrency.getDecimalPlaces());

        // 只统计"计入净资产"的账户，其余条件不设（概览不受账户管理页的筛选影响）
        AccountQueryForm form = new AccountQueryForm();
        form.setInclude(true);
        List<Account> accountList = accountRepository.findAll(buildSpec(userId, form));

        NetWorthSummary summary = new NetWorthSummary();
        if (accountList.isEmpty()) {
            summary.setTotalAssets(0L);
            summary.setTotalLiabilities(0L);
            summary.setNetWorth(0L);
            summary.setCurrencySymbol(baseCurrency.getSymbol());
            summary.setDecimalPlaces(baseCurrency.getDecimalPlaces());
            return summary;
        }

        long totalAssets = 0;
        long debtSum = 0;
        for (Account account : accountList) {
            Currency accountCurrency = currencyMap.get(account.getCurrency().getId());
            if (accountCurrency == null) {
                continue;
            }
            long baseBalance = convertToBaseCurrency(
                    account.getBalance(), accountCurrency, baseUsdRate, baseMultiplier);
            if (ASSET_TYPES.contains(account.getType())) {
                totalAssets += baseBalance;
            } else if (DEBT_TYPES.contains(account.getType())) {
                debtSum += baseBalance;
            }
        }

        summary.setTotalAssets(totalAssets);
        summary.setTotalLiabilities(-debtSum);
        summary.setNetWorth(totalAssets + debtSum);
        summary.setCurrencySymbol(baseCurrency.getSymbol());
        summary.setDecimalPlaces(baseCurrency.getDecimalPlaces());
        return summary;
    }

    /**
     * 资产/负债构成分布（本位币口径），供概览页饼图使用。
     *
     * 与 {@link #netWorth()} 同口径同范围：只统计 include=true 的账户，逐账户按 USD 锚点
     * 换算到本位币；余额为 0 的账户不参与占比（避免出现 0 面积扇区），负债类账户取相反数
     * 使负债规模恒为正。按金额降序返回，占比由前端图表自行计算。
     */
    @Transactional(readOnly = true)
    public BalanceDistribution balanceDistribution() {
        Long userId = CurrentUserHolder.get();
        Map<Long, Currency> currencyMap = loadCurrencyMap(userId);
        Currency baseCurrency = requireBaseCurrency(currencyMap);

        BigDecimal baseUsdRate = baseCurrency.getUsdRate();
        BigDecimal baseMultiplier = BigDecimal.TEN.pow(baseCurrency.getDecimalPlaces());

        AccountQueryForm form = new AccountQueryForm();
        form.setInclude(true);
        List<Account> accountList = accountRepository.findAll(buildSpec(userId, form));

        List<AccountChartItem> assets = new ArrayList<>();
        List<AccountChartItem> liabilities = new ArrayList<>();
        for (Account account : accountList) {
            Currency accountCurrency = currencyMap.get(account.getCurrency().getId());
            if (accountCurrency == null) {
                continue;
            }
            long baseBalance = convertToBaseCurrency(
                    account.getBalance(), accountCurrency, baseUsdRate, baseMultiplier);
            if (baseBalance == 0) {
                continue;
            }
            if (ASSET_TYPES.contains(account.getType())) {
                assets.add(new AccountChartItem(account.getName(), baseBalance));
            } else if (DEBT_TYPES.contains(account.getType())) {
                liabilities.add(new AccountChartItem(account.getName(), -baseBalance));
            }
        }
        assets.sort(Comparator.comparingLong(AccountChartItem::getAmount).reversed());
        liabilities.sort(Comparator.comparingLong(AccountChartItem::getAmount).reversed());

        BalanceDistribution distribution = new BalanceDistribution();
        distribution.setAssets(assets);
        distribution.setLiabilities(liabilities);
        distribution.setCurrencySymbol(baseCurrency.getSymbol());
        distribution.setDecimalPlaces(baseCurrency.getDecimalPlaces());
        return distribution;
    }

    /** 一次性加载当前用户所有币种并按 id 建索引，供本位币折算与账户币种查找复用。 */
    private Map<Long, Currency> loadCurrencyMap(Long userId) {
        List<Currency> allCurrencies = currencyRepository.findByUserId(userId);
        Map<Long, Currency> currencyMap = new HashMap<>();
        for (Currency c : allCurrencies) {
            currencyMap.put(c.getId(), c);
        }
        return currencyMap;
    }

    /**
     * 解析当前用户的本位币。未设置或记录悬空均属于数据契约破坏，统一抛
     * error.currency.base.missing；根因已在 CurrencyService.delete 堵住（禁止删除
     * 当前本位币），此处保留校验为纵深防御。
     */
    private Currency requireBaseCurrency(Map<Long, Currency> currencyMap) {
        Long baseCurrencyId = SessionManager.getCurrentUser().getBaseCurrencyId();
        if (baseCurrencyId == null) {
            throw new ResourceInvalidException("error.currency.base.missing");
        }
        Currency baseCurrency = currencyMap.get(baseCurrencyId);
        if (baseCurrency == null) {
            throw new ResourceInvalidException("error.currency.base.missing");
        }
        return baseCurrency;
    }

    /**
     * 把某个账户在其自身币种下的金额，换算成本位币的「最小货币单位」金额（long）。
     *
     * 与 {@link #convertToBaseCurrencyBig} 的分工：本方法负责「收口」——把上一阶段算出的
     * 本位币主单位金额乘以 baseMultiplier（10^本位币小数位）放大为最小单位，再四舍五入成整数。
     *
     * @param amountMinor    源币种的最小货币单位金额（如人民币的「分」、越南盾的「盾」）
     * @param fromCurrency   源币种，用于取其 decimalPlaces 与 usdRate
     * @param baseUsdRate    本位币基于 USD 的汇率（1 USD = baseUsdRate 个本位币）
     * @param baseMultiplier 10^本位币小数位，把本位币主单位金额放大为最小单位金额
     * @return 本位币下的最小货币单位金额，按 HALF_UP 舍入到整数 long
     */
    private long convertToBaseCurrency(long amountMinor, Currency fromCurrency,
                                       BigDecimal baseUsdRate, BigDecimal baseMultiplier) {
        return convertToBaseCurrencyBig(amountMinor, fromCurrency, baseUsdRate)
                .multiply(baseMultiplier)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    /**
     * 把某个账户在其自身币种下的金额，换算成本位币的「主单位」金额（返回 BigDecimal，不落 long）。
     *
     * 换算以 USD 为中间锚点，分三步：源币种主单位 → USD → 本位币主单位。
     * 每一步除法的商都保留 SCALE(8) 位小数（HALF_UP），避免中途截断引入误差，最终舍入统一由
     * {@link #convertToBaseCurrency} 收口。
     *
     * 三步说明：
     *   1. amountMinor ÷ 10^fromCurrency.decimalPlaces：最小单位还原为源币种主单位
     *      （如人民币 1050 分 → 10.50 元）。
     *   2. ÷ fromCurrency.usdRate：得到等值 USD。usdRate 语义是「1 USD 可换多少该币种」，
     *      故源币种金额除以 usdRate 即为 USD 金额。
     *   3. × baseUsdRate：从 USD 换成本位币的主单位金额。
     *
     * @param amountMinor  源币种的最小货币单位金额
     * @param fromCurrency 源币种
     * @param baseUsdRate  本位币基于 USD 的汇率（1 USD = baseUsdRate 个本位币）
     * @return 本位币的主单位金额（保留中间精度，尚未舍入到最小单位）
     */
    private BigDecimal convertToBaseCurrencyBig(long amountMinor, Currency fromCurrency, BigDecimal baseUsdRate) {
        BigDecimal amount = BigDecimal.valueOf(amountMinor)
                .divide(BigDecimal.TEN.pow(fromCurrency.getDecimalPlaces()), SCALE, RoundingMode.HALF_UP);
        return amount
                .divide(fromCurrency.getUsdRate(), SCALE, RoundingMode.HALF_UP)
                .multiply(baseUsdRate);
    }

}
