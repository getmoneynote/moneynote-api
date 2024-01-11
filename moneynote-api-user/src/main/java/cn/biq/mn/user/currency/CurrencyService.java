package cn.biq.mn.user.currency;

import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.utils.WebUtils;
import cn.biq.mn.user.bean.ApplicationScopeBean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final ApplicationScopeBean applicationScopeBean;
    private final WebUtils webUtils;

    @Transactional(readOnly = true)
    public List<CurrencyDetails> queryAll() {
        return applicationScopeBean.getCurrencyDetailsList();
    }

    @Transactional(readOnly = true)
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
    private BigDecimal convert(String fromCode, String toCode) {
        List<CurrencyDetails> currencyList = applicationScopeBean.getCurrencyDetailsList();
        CurrencyDetails fromCurrency = currencyList.stream().filter(currencyDetails -> fromCode.equals(currencyDetails.getName())).findAny().orElseThrow(ItemNotFoundException::new);
        CurrencyDetails toCurrency = currencyList.stream().filter(currencyDetails -> toCode.equals(currencyDetails.getName())).findAny().orElseThrow(ItemNotFoundException::new);
        BigDecimal fromRate = BigDecimal.valueOf(fromCurrency.getRate());
        BigDecimal toRate = BigDecimal.valueOf(toCurrency.getRate());
        return toRate.divide(fromRate, 2, RoundingMode.CEILING);
    }

    @Transactional(readOnly = true)
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

}
