package cn.biq.mn.currency;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.utils.WebUtils;
import cn.biq.mn.bean.ApplicationScopeBean;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final ApplicationScopeBean applicationScopeBean;
    private final WebUtils webUtils;

    public List<CurrencyDetails> queryAll() {
        return applicationScopeBean.getCurrencyDetailsList();
    }

    public Page<CurrencyDetails> query(CurrencyQueryForm query, Pageable page) {
        List<CurrencyDetails> currencyList = applicationScopeBean.getCurrencyDetailsList();
        List<CurrencyDetails> filteredList = currencyList;
        if (StringUtils.hasText(query.getName())) {
            filteredList = currencyList.stream()
                    .filter(person -> person.getName().contains(query.getName()))
                    .toList();
        }
        if (StringUtils.hasText(query.getBase())) {
            filteredList.forEach(i -> {
                i.setRate2(convert(query.getBase(), i.getName()).doubleValue());
            });
        }
        if (!page.getSort().isEmpty() && filteredList.size() > 1) {
            for (Sort.Order order  : page.getSort()) {
                if ("rate2".equals(order.getProperty())) {
                    if ("ASC".equals(order.getDirection().toString())) {
                        filteredList.sort(new Comparator<CurrencyDetails>() {
                            @Override
                            public int compare(CurrencyDetails c1, CurrencyDetails c2) {
                                return c1.getRate2().compareTo(c2.getRate2());
                            }
                        });
                    }
                    if ("DESC".equals(order.getDirection().toString())) {
                        filteredList.sort(new Comparator<CurrencyDetails>() {
                            @Override
                            public int compare(CurrencyDetails c1, CurrencyDetails c2) {
                                return c2.getRate2().compareTo(c1.getRate2());
                            }
                        });
                    }
                }
            }
        }
        List<CurrencyDetails> pageList;
        int start = page.getPageNumber() * page.getPageSize();
        int end = Math.min(start + page.getPageSize(), filteredList.size());
        if (start < end) {
            pageList = filteredList.subList(start, end);
        } else {
            pageList = new ArrayList<>();
        }
        return new PageImpl<>(pageList, page, filteredList.size());
    }

    public void checkCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new FailureMessageException("valid.fail");
        }
        List<CurrencyDetails> currencyList = applicationScopeBean.getCurrencyDetailsList();
        List<String> codeList = currencyList.stream().map(CurrencyDetails::getName).toList();
        if (!codeList.contains(code)) {
            throw new FailureMessageException("valid.fail");
        }
    }

    // TODO 定时任务，数据存入缓存
    // TODO 优化，不要每次都查数据库
    public BigDecimal convert(String fromCode, String toCode) {
        List<CurrencyDetails> currencyList = applicationScopeBean.getCurrencyDetailsList();
        CurrencyDetails fromCurrency = currencyList.stream().filter(currencyDetails -> fromCode.equals(currencyDetails.getName())).findAny().orElseThrow(ItemNotFoundException::new);
        CurrencyDetails toCurrency = currencyList.stream().filter(currencyDetails -> toCode.equals(currencyDetails.getName())).findAny().orElseThrow(ItemNotFoundException::new);
        BigDecimal fromRate = BigDecimal.valueOf(fromCurrency.getRate());
        BigDecimal toRate = BigDecimal.valueOf(toCurrency.getRate());
        return toRate.divide(fromRate, 20, RoundingMode.CEILING);
    }

    public BigDecimal convert(BigDecimal amount, String fromCode, String toCode) {
        if (fromCode.equals(toCode)) {
            return amount;
        }
        return amount.multiply(convert(fromCode, toCode)).setScale(2, RoundingMode.CEILING);
    }

    public boolean refreshCurrency() {
        try {
            HashMap<String, Object> resMap =  webUtils.get("https://api.exchangerate-api.com/v4/latest/USD");
            var ratesMap = (Map<String, Number>) resMap.get("rates");
            List<CurrencyDetails> currencyDetailsList = applicationScopeBean.getCurrencyDetailsList();
            ratesMap.forEach((key, value) -> {
                Optional<CurrencyDetails> currencyDetailsOptional = currencyDetailsList.stream()
                        .filter(user -> user.getName().equals(key))
                        .findFirst();
                if (currencyDetailsOptional.isPresent()) {
                    var currencyDetails = currencyDetailsOptional.get();
                    currencyDetails.setRate(value.doubleValue());
                }
            });
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public BigDecimal calc(String from, String to, BigDecimal amount) {
        BigDecimal rate = convert(from, to);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }

}
