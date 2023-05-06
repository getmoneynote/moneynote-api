package cn.biq.mn.user.currency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public List<CurrencyDetails> queryAll() {
        List<Currency> entityList = currencyRepository.findAllByEnable(true);
        return entityList.stream().map(CurrencyMapper::toDetails).toList();
    }

    @Transactional(readOnly = true)
    public void checkCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new FailureMessageException("valid.fail");
        }
        List<Currency> currencyList = currencyRepository.findAllByEnable(true);
        List<String> codeList = currencyList.stream().map(Currency::getName).toList();
        if (!codeList.contains(code)) {
            throw new FailureMessageException("valid.fail");
        }
    }

    // TODO 定时任务，数据存入缓存
    // TODO 优化，不要每次都查数据库
    private BigDecimal convert(String fromCode, String toCode) {
        Currency fromCurrency = currencyRepository.findOneByName(fromCode).orElseThrow(ItemNotFoundException::new);
        Currency toCurrency = currencyRepository.findOneByName(toCode).orElseThrow(ItemNotFoundException::new);
        BigDecimal fromRate = fromCurrency.getRate();
        BigDecimal toRate = toCurrency.getRate();
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
