package cn.biq.mn.admin.currency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

}
