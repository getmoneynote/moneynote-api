package cn.biq.mn.user.currency;

import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.user.bean.ApplicationScopeBean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final ApplicationScopeBean applicationScopeBean;

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

}
