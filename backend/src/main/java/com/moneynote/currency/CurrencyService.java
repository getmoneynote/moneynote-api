package com.moneynote.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.exception.ResourceDuplicateException;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.exception.ResourceProtectedException;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.SessionManager;
import com.moneynote.user.UserRepository;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final int SCALE = 8;

    private static final Set<String> PROTECTED_CODES = Set.of("USD", "CNY");

    private final CurrencyRepository currencyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(CurrencyCreateForm form) {
        Long userId = CurrentUserHolder.get();
        if (currencyRepository.existsByUserIdAndCode(userId, form.getCode())) {
            throw new ResourceDuplicateException("resource.currency", form.getCode());
        }
        Currency entity = new Currency();
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setCode(form.getCode());
        entity.setName(form.getName());
        entity.setSymbol(form.getSymbol());
        entity.setDecimalPlaces(form.getDecimalPlaces());
        entity.setUsdRate(form.getUsdRate());
        currencyRepository.save(entity);
    }

    @Transactional
    public void update(Long id, CurrencyUpdateForm form) {
        Long userId = CurrentUserHolder.get();
        Currency entity = currencyRepository.findByIdAndUserId(id, userId).orElse(null);
        if (entity == null) {
            throw new ResourceNotFoundException("resource.currency");
        }
        boolean isUsd = "USD".equals(entity.getCode());
        if (isUsd) {
            boolean rateChanged = form.getUsdRate() != null && entity.getUsdRate().compareTo(form.getUsdRate()) != 0;
            if (rateChanged) {
                throw new ResourceProtectedException("error.currency.update.usd");
            }
            if (form.getName() != null) {
                entity.setName(form.getName());
            }
        } else {
            if (form.getName() != null) {
                entity.setName(form.getName());
            }
            if (form.getUsdRate() != null) {
                entity.setUsdRate(form.getUsdRate());
            }
        }
        // entity 为持久态对象，事务提交时自动 flush，无需显式 save
    }

    @Transactional
    public void delete(Long id) {
        Long userId = CurrentUserHolder.get();
        // 当前本位币不允许删除：否则 baseCurrencyId 悬空，所有按本位币折算的统计都会失效。
        // 用户应先切换本位币，再删除旧币种（与 PROTECTED_CODES 的系统币保护是两道独立的闸）
        if (id.equals(SessionManager.getCurrentUser().getBaseCurrencyId())) {
            throw new ResourceProtectedException("error.currency.remove.base");
        }
        currencyRepository.findByIdAndUserId(id, userId).ifPresent(entity -> {
            if (PROTECTED_CODES.contains(entity.getCode())) {
                throw new ResourceProtectedException("error.currency.remove.protected");
            }
            currencyRepository.delete(entity);
        });
    }

    @Transactional(readOnly = true)
    public List<CurrencyOption> listOptions() {
        List<CurrencyOption> options = new ArrayList<>();
        Long currentUserId = CurrentUserHolder.get();
        // 排序保证跨数据库稳定（无 ORDER BY 时各数据库返回顺序不定），按 code 升序
        List<Currency> currencyList = currencyRepository.findByUserId(currentUserId, Sort.by(Sort.Order.asc("code")));
        currencyList.forEach(item -> {
            CurrencyOption option = new CurrencyOption();
            option.setValue(item.getId());
            option.setLabel(item.getName());
            option.setCode(item.getCode());
            option.setSymbol(item.getSymbol());
            option.setDecimalPlaces(item.getDecimalPlaces());
            options.add(option);
        });
        return options;
    }

    @Transactional(readOnly = true)
    public Page<CurrencyListItem> list(CurrencyQueryForm form, Pageable page) {
        Long userId = CurrentUserHolder.get();
        final BigDecimal baseUsdRate;
        if (form.getBase() == null) {
            baseUsdRate = BigDecimal.ONE;
        } else {
            Currency baseCurrency = currencyRepository.findByIdAndUserId(form.getBase(), userId)
                    .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
            baseUsdRate = baseCurrency.getUsdRate();
        }
        Long baseCurrencyId = SessionManager.getCurrentUser().getBaseCurrencyId();
        // 无 ORDER BY 的分页翻页会漂移，未指定排序时兜底按 code 升序（code 对用户唯一，排序稳定）
        Pageable sortedPage = page.getSort().isSorted()
                ? page
                : PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by(Sort.Order.asc("code")));
        Page<Currency> entityPage = currencyRepository.findByUserId(userId, sortedPage);
        return entityPage.map(item -> {
            CurrencyListItem listItem = new CurrencyListItem();
            listItem.setId(item.getId());
            listItem.setName(item.getName());
            listItem.setCode(item.getCode());
            listItem.setSymbol(item.getSymbol());
            listItem.setDecimalPlaces(item.getDecimalPlaces());
            listItem.setUsdRate(item.getUsdRate());
            listItem.setBaseRate(item.getUsdRate().divide(baseUsdRate, 8, RoundingMode.HALF_UP));
            listItem.setIsBase(Objects.equals(item.getId(), baseCurrencyId));
            listItem.setDeletable(!PROTECTED_CODES.contains(item.getCode()));
            listItem.setRateEditable(!"USD".equals(item.getCode()));
            return listItem;
        });
    }

    /**
     * 把「源币种的最小单位金额」换算成「目标币种的最小单位金额」。
     *
     * USD 锚点算法：源币种主单位 → USD → 目标币种主单位，中间保留 {@link #SCALE} 位小数，
     * 最后乘目标币种小数位并 HALF_UP 收整为 long。MoneyFlowService（流水换算）、
     * AccountService（余额调整差额换算）共用此方法；AccountService 的「本位币展示换算」
     * 是本算法的特例（目标币种固定为本位币），保留其再上层舍入口径，不冲突。
     */
    public long convert(long amountMinor, Currency from, Currency to) {
        BigDecimal major = BigDecimal.valueOf(amountMinor)
                .divide(BigDecimal.TEN.pow(from.getDecimalPlaces()), SCALE, RoundingMode.HALF_UP)
                .divide(from.getUsdRate(), SCALE, RoundingMode.HALF_UP)
                .multiply(to.getUsdRate());
        return major.multiply(BigDecimal.TEN.pow(to.getDecimalPlaces()))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

}
