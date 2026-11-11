package com.moneynote.moneyflow;

import com.moneynote.account.Account;
import com.moneynote.account.AccountRepository;
import com.moneynote.base.IdAndNameDetails;
import com.moneynote.book.Book;
import com.moneynote.book.BookService;
import com.moneynote.category.Category;
import com.moneynote.category.CategoryRepository;
import com.moneynote.currency.Currency;
import com.moneynote.currency.CurrencyRepository;
import com.moneynote.currency.CurrencyService;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.payee.Payee;
import com.moneynote.payee.PayeeRepository;
import com.moneynote.tag.Tag;
import com.moneynote.tag.TagRepository;
import com.moneynote.tagrelation.TagRelation;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.SessionManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoneyFlowService {

    private static final int TYPE_EXPENSE = 1;
    private static final int TYPE_INCOME = 2;
    private static final int TYPE_TRANSFER = 3;

    private final MoneyFlowRepository moneyFlowRepository;
    private final BookService bookService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PayeeRepository payeeRepository;
    private final TagRepository tagRepository;
    private final CurrencyService currencyService;
    private final CurrencyRepository currencyRepository;

    @Transactional
    public void create(MoneyFlowCreateForm form) {
        Long userId = CurrentUserHolder.get();
        Book book = bookService.resolveBook(form.getBookId());
        Integer type = form.getType();
        validateAmountSign(form, type);

        // 支出/收入可空（未归属），转账必填；空时不更新账户余额
        Account account = resolveAccount(form, userId, type);

        Category category = resolveCategory(form, book, type);
        Account to = resolveToAccount(form, userId, account, type);
        Payee payee = resolvePayee(form, book);
        long convertedAmount = resolveConvertedAmount(form, account, to, book);

        MoneyFlow entity = new MoneyFlow();
        entity.setBook(book);
        entity.setType(type);
        entity.setAmount(form.getAmount());
        entity.setConvertedAmount(convertedAmount);
        entity.setAccount(account);
        entity.setCreateTime(form.getCreateTime());
        entity.setTitle(form.getTitle());
        entity.setNotes(form.getNotes());
        entity.setCategory(category);
        entity.setTo(to);
        entity.setPayee(payee);
        entity.setConfirm(form.getConfirm());
        entity.setInclude(form.getInclude());
        resolveTags(form, book, entity);

        moneyFlowRepository.save(entity);

        if (Boolean.TRUE.equals(form.getConfirm())) {
            applyBalance(type, account, to, form.getAmount(), convertedAmount);
        }


    }

    /**
     * 更新（全量替换；type 创建后不可变）：先撤销旧流水的余额影响，再按新表单重算并重新应用。
     * 复用 {@link MoneyFlowCreateForm} 的全体现字段，type 取自既有实体（忽略表单传值，避免转账/收支语义被改写）。
     */
    @Transactional
    public void update(Long id, MoneyFlowCreateForm form) {
        Long userId = CurrentUserHolder.get();
        MoneyFlow entity = moneyFlowRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.moneyFlow"));

        // 账本创建后不可变（同 old 实现），按既有账本重算分类/标签/交易对象/币种
        Book book = entity.getBook();
        Integer type = entity.getType();
        validateAmountSign(form, type);

        // 先回滚旧余额，再套用新值，避免旧额/新额叠加或账户切换后残留
        if (Boolean.TRUE.equals(entity.getConfirm())) {
            refundBalance(type, entity.getAccount(), entity.getTo(),
                    entity.getAmount(), entity.getConvertedAmount());
        }

        Account account = resolveAccount(form, userId, type);
        Category category = resolveCategory(form, book, type);
        Account to = resolveToAccount(form, userId, account, type);
        Payee payee = resolvePayee(form, book);
        long convertedAmount = resolveConvertedAmount(form, account, to, book);

        entity.setAmount(form.getAmount());
        entity.setConvertedAmount(convertedAmount);
        entity.setAccount(account);
        entity.setCreateTime(form.getCreateTime());
        entity.setTitle(form.getTitle());
        entity.setNotes(form.getNotes());
        entity.setCategory(category);
        entity.setTo(to);
        entity.setPayee(payee);
        entity.setConfirm(form.getConfirm());
        entity.setInclude(form.getInclude());

        // 标签全量重建：clear 触发 orphanRemoval 删除旧关联，再按新表单逐个关联；实体已托管，随事务提交 flush
        entity.getTags().clear();
        resolveTags(form, book, entity);

        if (Boolean.TRUE.equals(form.getConfirm())) {
            applyBalance(type, account, to, form.getAmount(), convertedAmount);
        }
    }

    /**
     * 删除账单：已确认的账单删除前先撤销其对账户余额的影响（refundBalance 对 type=4 余额调整天然 no-op，
     * 只删历史痕迹、不动账户余额），再删实体；标签关联随 cascade + orphanRemoval 一并清理。
     * 未找到时静默忽略（幂等，同 currency.delete 的 ifPresent 写法）。
     */
    @Transactional
    public void delete(Long id) {
        Long userId = CurrentUserHolder.get();
        moneyFlowRepository.findByIdAndUserId(id, userId).ifPresent(entity -> {
            if (Boolean.TRUE.equals(entity.getConfirm())) {
                refundBalance(entity.getType(), entity.getAccount(), entity.getTo(),
                        entity.getAmount(), entity.getConvertedAmount());
            }
            moneyFlowRepository.delete(entity);
        });
    }

    @Transactional(readOnly = true)
    public Page<MoneyFlowListItem> query(MoneyFlowQueryForm form, Pageable page) {
        Long userId = CurrentUserHolder.get();
        // 只有当 minAmount/maxAmount 非空时才需要账本币种精度；否则避免无谓的 DB 查询
        Currency amountFilterCurrency = (form.getMinAmount() != null || form.getMaxAmount() != null)
                ? resolveAmountFilterCurrency(form)
                : null;
        Specification<MoneyFlow> spec = buildSpec(form, userId, amountFilterCurrency);
        if (form.getType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), form.getType()));
        }
        Page<MoneyFlow> entityPage = moneyFlowRepository.findAll(spec, page);
        return entityPage.map(this::toListItem);
    }

    /**
     * 与列表同一套条件（忽略 type），分别汇总支出/收入，结余由后端计算。
     * 金额统一换算到用户「本位币」，与账本币种无关（每条流水按自身原币换成本位币后累加）。
     */
    @Transactional(readOnly = true)
    public MoneyFlowSummary statistics(MoneyFlowQueryForm form) {
        Long userId = CurrentUserHolder.get();

        MoneyFlowSummary summary = new MoneyFlowSummary();
        // 一次性加载所有币种（含本位币），避免逐条查库；本位币从 map 取，不单独查询
        Map<Long, Currency> currencyMap = loadCurrencyMap(userId);
        Currency baseCurrency = requireBaseCurrency(currencyMap);

        // 金额区间过滤需要账本币种精度；只取一次并传入 buildSpec，避免其内部重复查库
        // 只有当 minAmount/maxAmount 非空时才需要账本币种精度
        Currency amountFilterCurrency = (form.getMinAmount() != null || form.getMaxAmount() != null)
                ? resolveAmountFilterCurrency(form)
                : null;
        Specification<MoneyFlow> spec = buildSpec(form, userId, amountFilterCurrency);

        // 使用分组求和替代全量 findAll，避免大数据量下 OOM
        // 先在数据库层按 currency_id 分组求和（只返回 currency_id + amount_sum），
        // 再在 Java 层对每组结果做换算后累加。查询返回的组数等于实际使用的币种数，远少于流水条目数。
        List<Object[]> expenseGrouped = moneyFlowRepository.sumAmountGroupedByCurrency(spec, TYPE_EXPENSE);
        long expense = convertGroupedSum(expenseGrouped, currencyMap, baseCurrency);

        List<Object[]> incomeGrouped = moneyFlowRepository.sumAmountGroupedByCurrency(spec, TYPE_INCOME);
        long income = convertGroupedSum(incomeGrouped, currencyMap, baseCurrency);

        summary.setExpense(expense);
        summary.setIncome(income);
        summary.setSurplus(income - expense);
        summary.setCurrencyCode(baseCurrency.getCode());
        summary.setCurrencySymbol(baseCurrency.getSymbol());
        summary.setCurrencyDecimalPlaces(baseCurrency.getDecimalPlaces());
        return summary;
    }

    /**
     * 按分类汇总支出或收入（概览页支出/收入分类环形图）。
     *
     * 过滤条件与列表完全一致（type 必填且仅限 1=支出 / 2=收入，confirm/include 由调用方决定）；
     * 分组维度为「根分类」——叶子分类的金额沿分类树归并到所属根分类，跨账本的同名根分类
     * 合并为一项（与列表/统计一样以用户为口径，不强制账本）。
     * 金额统一换算到用户「本位币」，与账本币种无关（每条流水按自身原币换算后累加）。
     */
    @Transactional(readOnly = true)
    public CategoryStatistics categoryStatistics(MoneyFlowQueryForm form) {
        Long userId = CurrentUserHolder.get();
        Integer type = form.getType();
        if (type == null || (type != TYPE_EXPENSE && type != TYPE_INCOME)) {
            throw new ResourceInvalidException("error.invalid.argument");
        }

        Map<Long, Currency> currencyMap = loadCurrencyMap(userId);
        Currency baseCurrency = requireBaseCurrency(currencyMap);

        // 金额区间过滤需要账本币种精度；只有当 minAmount/maxAmount 非空时才需要
        Currency amountFilterCurrency = (form.getMinAmount() != null || form.getMaxAmount() != null)
                ? resolveAmountFilterCurrency(form)
                : null;
        Specification<MoneyFlow> spec = buildSpec(form, userId, amountFilterCurrency);

        // 加载当前用户该类型下的全部分类，构建 id → 实体映射，用于叶子 → 根分类归并
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category c : categoryRepository.findByBook_UserIdAndType(userId, type)) {
            categoryMap.put(c.getId(), c);
        }

        // DB 按 (叶子分类, 币种) 分组求和（组数远少于流水条数），Java 层换算本位币后归并到根分类名；
        // 层级归并无法用一条 SQL 表达，故分组只到叶子，归并在内存完成（分类数量级很小）
        Map<String, Long> summedByRoot = new HashMap<>();
        for (Object[] row : moneyFlowRepository.sumAmountGroupedByCategoryAndCurrency(spec, type)) {
            Long categoryId = (Long) row[0];
            Long currencyId = (Long) row[1];
            Long sumAmount = (Long) row[2];
            if (categoryId == null || sumAmount == null || sumAmount == 0L) continue;
            Category category = categoryMap.get(categoryId);
            if (category == null) continue;
            Currency currency = currencyMap.get(currencyId);
            if (currency == null) continue;
            long converted = currencyId.equals(baseCurrency.getId())
                    ? sumAmount
                    : currencyService.convert(sumAmount, currency, baseCurrency);
            summedByRoot.merge(rootOf(category, categoryMap).getName(), converted, Long::sum);
        }

        // 跳过 0 项、按金额降序；总额为 0 时返回空列表（前端展示空态）
        List<CategoryChartItem> items = summedByRoot.entrySet().stream()
                .filter(e -> e.getValue() != 0L)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new CategoryChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        CategoryStatistics result = new CategoryStatistics();
        result.setCategories(items);
        result.setCurrencyCode(baseCurrency.getCode());
        result.setCurrencySymbol(baseCurrency.getSymbol());
        result.setCurrencyDecimalPlaces(baseCurrency.getDecimalPlaces());
        return result;
    }

    /** 沿父链上溯到根分类；parent 必在映射内（同一用户的分类集合），环/悬空时停在当前节点。 */
    private Category rootOf(Category category, Map<Long, Category> categoryMap) {
        Category current = category;
        Set<Long> visited = new HashSet<>();
        while (current.getParent() != null && visited.add(current.getId())) {
            Category parent = categoryMap.get(current.getParent().getId());
            if (parent == null) break;
            current = parent;
        }
        return current;
    }

    /** 一次性加载用户全部币种（含本位币）为 id → 实体映射，避免逐条查库。 */
    private Map<Long, Currency> loadCurrencyMap(Long userId) {
        Map<Long, Currency> currencyMap = new HashMap<>();
        for (Currency c : currencyRepository.findByUserId(userId)) {
            currencyMap.put(c.getId(), c);
        }
        return currencyMap;
    }

    /**
     * 本位币校验与解析：baseCurrencyId 为 null 或币种记录悬空都属于数据契约破坏
     * （注册 initData 必设、根因已在 CurrencyService.delete 堵住：禁止删除当前本位币），
     * 不能静默按 0 展示。
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

    /** 将分组求和的结果（currencyId + amountSum）按本位币汇总换算。 */
    private long convertGroupedSum(List<Object[]> grouped, Map<Long, Currency> currencyMap, Currency baseCurrency) {
        long total = 0L;
        for (Object[] row : grouped) {
            Long currencyId = (Long) row[0];
            Long sumAmount = (Long) row[1];
            if (sumAmount == 0L) continue;
            Currency currency = currencyMap.get(currencyId);
            if (currency == null) continue;
            if (currencyId.equals(baseCurrency.getId())) {
                total += sumAmount;
            } else {
                total += currencyService.convert(sumAmount, currency, baseCurrency);
            }
        }
        return total;
    }

    /**
     * 拼接除 type 外的公共过滤条件；列表在此基础上额外叠加 type，统计则固定按 type 分别求和。
     * 标签为集合，用 EXISTS 子查询匹配（任一命中即满足），避免 join 到集合导致分页 count 重复。
     *
     * @param amountFilterCurrency 可选的账本币种，用于精度换算；若为 null 则调用 resolveAmountFilterCurrency(form) 获取
     */
    private Specification<MoneyFlow> buildSpec(MoneyFlowQueryForm form, Long userId, Currency amountFilterCurrency) {
        Long bookId = form.getBookId();
        // 只有当 minAmount/maxAmount 非空时才需要计算精度
        final Long minMinor;
        final Long maxMinor;
        if (form.getMinAmount() != null || form.getMaxAmount() != null) {
            Currency currencyToUse = amountFilterCurrency != null ? amountFilterCurrency : resolveAmountFilterCurrency(form);
            BigDecimal multiplier = BigDecimal.TEN.pow(currencyToUse.getDecimalPlaces());
            minMinor = toMinor(form.getMinAmount(), multiplier, RoundingMode.FLOOR);
            maxMinor = toMinor(form.getMaxAmount(), multiplier, RoundingMode.CEILING);
        } else {
            minMinor = null;
            maxMinor = null;
        }

        List<Long> categoryIds = parseIds(form.getCategoryIds());
        List<Long> payeeIds = parseIds(form.getPayeeIds());
        List<Long> tagIds = parseIds(form.getTagIds());

        return (root, query, cb) -> {
            // 账本/用户隔离：流水无直接 user 关联，经 book.user 收敛；选了账本再追加账本等值
            Predicate p = cb.equal(root.get("book").get("user").get("id"), userId);
            if (bookId != null) {
                p = cb.and(p, cb.equal(root.get("book").get("id"), bookId));
            }

            if (form.getTitle() != null && !form.getTitle().isBlank()) {
                String kw = "%" + form.getTitle().trim().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.<String>get("title")), kw));
            }
            if (form.getNotes() != null && !form.getNotes().isBlank()) {
                String kw = "%" + form.getNotes().trim().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.<String>get("notes")), kw));
            }
            if (minMinor != null) {
                p = cb.and(p, cb.ge(root.<Long>get("convertedAmount"), minMinor));
            }
            if (maxMinor != null) {
                p = cb.and(p, cb.le(root.<Long>get("convertedAmount"), maxMinor));
            }
            if (form.getMinTime() != null) {
                p = cb.and(p, cb.ge(root.<Long>get("createTime"), form.getMinTime()));
            }
            if (form.getMaxTime() != null) {
                p = cb.and(p, cb.le(root.<Long>get("createTime"), form.getMaxTime()));
            }
            if (form.getAccountId() != null || form.getToAccountId() != null) {
                Join<MoneyFlow, Account> accountJoin = root.join("account", JoinType.LEFT);
                Join<MoneyFlow, Account> toJoin = root.join("to", JoinType.LEFT);
                if (form.getAccountId() != null) {
                    p = cb.and(p, cb.or(
                            cb.equal(accountJoin.get("id"), form.getAccountId()),
                            cb.equal(toJoin.get("id"), form.getAccountId())));
                }
                if (form.getToAccountId() != null) {
                    p = cb.and(p, cb.equal(toJoin.get("id"), form.getToAccountId()));
                }
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                p = cb.and(p, root.get("category").get("id").in(categoryIds));
            }
            if (payeeIds != null && !payeeIds.isEmpty()) {
                p = cb.and(p, root.get("payee").get("id").in(payeeIds));
            }
            if (tagIds != null && !tagIds.isEmpty()) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<TagRelation> tr = sub.from(TagRelation.class);
                sub.select(tr.<Long>get("id"))
                        .where(cb.equal(tr.get("moneyFlow"), root),
                                tr.get("tag").get("id").in(tagIds));
                p = cb.and(p, cb.exists(sub));
            }
            if (form.getConfirm() != null) {
                p = cb.and(p, cb.equal(root.get("confirm"), form.getConfirm()));
            }
            if (form.getInclude() != null) {
                p = cb.and(p, cb.equal(root.get("include"), form.getInclude()));
            }
            return p;
        };
    }

    /**
     * 对满足条件的流水按类型求和，金额换算到本位币（最小单位）后累加。
     *
     * 每条流水的真实金额是 amount（有账户=账户币种，无账户=账本币种），
     * 经 USD 锚点换算成本位币后再求和；不再用 convertedAmount（账本币种）汇总，
     * 因此结果与本位币一致、与账本币种无关。
     */
    private long sumInBaseCurrency(Specification<MoneyFlow> spec, int type,
                                   Map<Long, Currency> currencyMap, Currency baseCurrency) {
        Specification<MoneyFlow> typeSpec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        List<MoneyFlow> flows = moneyFlowRepository.findAll(typeSpec);
        long total = 0L;
        for (MoneyFlow flow : flows) {
            Currency sourceCurrency = resolveSourceCurrency(flow, currencyMap);
            if (sourceCurrency == null) {
                continue;
            }
            // 原币即本位币时直接取 amount，避免多余换算引入舍入（同 AccountService 的本位币特例）
            if (sourceCurrency.getId().equals(baseCurrency.getId())) {
                total += flow.getAmount();
            } else {
                total += currencyService.convert(flow.getAmount(), sourceCurrency, baseCurrency);
            }
        }
        return total;
    }

    /** 流水的原币种：有账户取账户币种，无账户取账本币种（与 amount 计量口径一致）。 */
    private Currency resolveSourceCurrency(MoneyFlow flow, Map<Long, Currency> currencyMap) {
        if (flow.getAccount() != null) {
            return currencyMap.get(flow.getAccount().getCurrency().getId());
        }
        return currencyMap.get(flow.getBook().getCurrency().getId());
    }

    /**
     * 只读视图映射；在 readOnly 事务内懒加载单值关联（一对一/多对一），避免导出实体。
     * 个人/小企业规模下单页行数有限，逐条懒加载可接受，后续若需可再针对 tags 批处理。
     */
    private MoneyFlowListItem toListItem(MoneyFlow flow) {
        MoneyFlowListItem item = new MoneyFlowListItem();
        item.setId(flow.getId());
        Book book = flow.getBook();
        item.setBookId(book.getId());
        item.setBookName(book.getName());
        item.setType(flow.getType());
        item.setTitle(flow.getTitle());
        item.setNotes(flow.getNotes());
        item.setAmount(flow.getAmount());
        item.setConvertedAmount(flow.getConvertedAmount());
        item.setCreateTime(flow.getCreateTime());
        item.setConfirm(flow.getConfirm());
        item.setInclude(flow.getInclude());

        Account account = flow.getAccount();
        Account to = flow.getTo();

        Currency currency = book.getCurrency();
        Currency amountCurrency = account != null ? account.getCurrency() : currency;
        item.setAmountCurrencySymbol(amountCurrency.getSymbol());
        item.setAmountCurrencyDecimalPlaces(amountCurrency.getDecimalPlaces());

        // 换算金额币种与 resolveConvertedAmount 的目标币种一致：转账=转入账户，收支=账本
        Currency convertedCurrency = to != null ? to.getCurrency() : currency;
        item.setConvertedCurrencySymbol(convertedCurrency.getSymbol());
        item.setConvertedCurrencyDecimalPlaces(convertedCurrency.getDecimalPlaces());
        item.setNeedConvert(!Objects.equals(amountCurrency.getId(), convertedCurrency.getId()));

        if (account != null) {
            item.setAccountId(account.getId());
            item.setAccountName(account.getName());
        }
        if (to != null) {
            item.setToAccountId(to.getId());
            item.setToAccountName(to.getName());
        }

        Category category = flow.getCategory();
        if (category != null) {
            item.setCategoryId(category.getId());
            item.setCategoryName(category.getName());
        }

        Payee payee = flow.getPayee();
        if (payee != null) {
            item.setPayeeId(payee.getId());
            item.setPayeeName(payee.getName());
        }

        if (flow.getTags() != null && !flow.getTags().isEmpty()) {
            List<IdAndNameDetails> tags = flow.getTags().stream()
                    .map(tr -> new IdAndNameDetails(tr.getTag().getId(), tr.getTag().getName()))
                    .sorted(Comparator.comparing(IdAndNameDetails::getName))
                    .collect(Collectors.toList());
            item.setTags(tags);
        }
        return item;
    }

    /**
     * 列表金额区间筛选的币种口径：选了账本用该账本币种；否则回退当前账本币种。
     * 仅用于 minAmount/maxAmount 与 convertedAmount（账本币种）比较时的精度换算，与统计本位币无关。
     */
    private Currency resolveAmountFilterCurrency(MoneyFlowQueryForm form) {
        return bookService.resolveBook(form.getBookId()).getCurrency();
    }

    private Long toMinor(BigDecimal value, BigDecimal multiplier, RoundingMode mode) {
        if (value == null) {
            return null;
        }
        return value.multiply(multiplier).setScale(0, mode).longValueExact();
    }

    /** 逗号分隔的 id 字符串 → List<Long>；空/非法项忽略，空串或 null 返回 null。 */
    private List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // 忽略非法项，避免整条查询失败
            }
        }
        return ids;
    }

    /** 主账户：支出/收入可空（未归属）、转账必填；传了就必须属于当前用户。 */
    private Account resolveAccount(MoneyFlowCreateForm form, Long userId, Integer type) {
        if (form.getAccountId() == null) {
            if (type == TYPE_TRANSFER) {
                throw new ResourceInvalidException("error.invalid.argument");
            }
            return null;
        }
        return accountRepository.findByIdAndUserId(form.getAccountId(), userId)
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
    }

    /** 分类：转账返回 null；支出/收入必填、必须属于当前账本、类型需与流水一致。 */
    private Category resolveCategory(MoneyFlowCreateForm form, Book book, Integer type) {
        if (type == TYPE_TRANSFER) {
            return null;
        }
        if (form.getCategoryId() == null) {
            throw new ResourceInvalidException("error.invalid.argument");
        }
        Category category = categoryRepository.findByIdAndBookId(form.getCategoryId(), book.getId())
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
        if (!type.equals(category.getType())) {
            throw new ResourceInvalidException("error.invalid.argument");
        }
        return category;
    }

    /**
     * 金额符号校验：支出/收入允许负数（负数 = 退款记录，applyBalance 的加减语义天然兼容），
     * 转账必须非负（转出/转入为负没有业务语义）。表单注解无法按 type 区分，收敛在这里。
     */
    private void validateAmountSign(MoneyFlowCreateForm form, Integer type) {
        if (type != TYPE_TRANSFER) {
            return;
        }
        if (form.getAmount() < 0
                || (form.getConvertedAmount() != null && form.getConvertedAmount() < 0)) {
            throw new ResourceInvalidException("error.moneyflow.transfer.amount.negative");
        }
    }

    /** 转入账户：非转账返回 null；转账必填、不能等于转出账户、必须属于当前用户。 */
    private Account resolveToAccount(MoneyFlowCreateForm form, Long userId, Account account, Integer type) {
        if (type != TYPE_TRANSFER) {
            return null;
        }
        if (form.getToAccountId() == null) {
            throw new ResourceInvalidException("error.invalid.argument");
        }
        if (form.getToAccountId().equals(account.getId())) {
            throw new ResourceInvalidException("error.moneyflow.to.account.same");
        }
        return accountRepository.findByIdAndUserId(form.getToAccountId(), userId)
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
    }

    /** 交易对象：可空；传了就必须属于当前账本。 */
    private Payee resolvePayee(MoneyFlowCreateForm form, Book book) {
        if (form.getPayeeId() == null) {
            return null;
        }
        return payeeRepository.findByIdAndBookId(form.getPayeeId(), book.getId())
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
    }

    /**
     * 换算金额：账户币种与目标币种一致时等于 amount（免填），不一致时必填（用户输入）。
     * 目标币种 = 转账的转入账户币种 / 收支的账本币种；account 为空时按账本币种计量，直接取 amount。
     */
    private long resolveConvertedAmount(MoneyFlowCreateForm form, Account account, Account to, Book book) {
        if (account == null) {
            return form.getAmount();
        }
        Currency targetCurrency = (to != null) ? to.getCurrency() : book.getCurrency();
        if (account.getCurrency().getId().equals(targetCurrency.getId())) {
            return form.getAmount();
        }
        if (form.getConvertedAmount() == null) {
            throw new ResourceInvalidException("error.moneyflow.converted.amount.required");
        }
        return form.getConvertedAmount();
    }

    /** 标签：逐个校验归属当前账本后，按实体上的 cascade 关联进流水（orphanRemoval 一并生效）。 */
    private void resolveTags(MoneyFlowCreateForm form, Book book, MoneyFlow entity) {
        if (form.getTagIds() == null || form.getTagIds().isEmpty()) {
            return;
        }
        for (Long tagId : form.getTagIds()) {
            Tag tag = tagRepository.findByIdAndBookId(tagId, book.getId())
                    .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
            TagRelation relation = new TagRelation();
            relation.setTag(tag);
            relation.setMoneyFlow(entity);
            entity.getTags().add(relation);
        }
    }

    /**
     * 确认后执行账户余额变动：支出 -，收入 +，转账「转出账户 -、转入账户 + 到账金额（convertedAmount）」。
     * account 为空（未归属）时跳过，仅记 confirm 状态、不动任何账户余额。
     * 账户发生变化的是持久态对象，事务提交自动 flush，无需显式 save。
     */
    private void applyBalance(Integer type, Account account, Account to, long amount, long convertedAmount) {
        if (account == null) {
            return;
        }
        if (type == TYPE_EXPENSE) {
            account.setBalance(account.getBalance() - amount);
        } else if (type == TYPE_INCOME) {
            account.setBalance(account.getBalance() + amount);
        } else if (type == TYPE_TRANSFER) {
            account.setBalance(account.getBalance() - amount);
            to.setBalance(to.getBalance() + convertedAmount);
        }
    }

    /** applyBalance 的逆操作：撤销一条已确认流水的余额影响，供更新时先回滚旧值。 */
    private void refundBalance(Integer type, Account account, Account to, long amount, long convertedAmount) {
        if (account == null) {
            return;
        }
        if (type == TYPE_EXPENSE) {
            account.setBalance(account.getBalance() + amount);
        } else if (type == TYPE_INCOME) {
            account.setBalance(account.getBalance() - amount);
        } else if (type == TYPE_TRANSFER) {
            account.setBalance(account.getBalance() + amount);
            to.setBalance(to.getBalance() - convertedAmount);
        }
    }

}