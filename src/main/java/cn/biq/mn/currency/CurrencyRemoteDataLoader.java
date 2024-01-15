package cn.biq.mn.currency;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@DependsOn("currencyDataLoader")
public class CurrencyRemoteDataLoader implements ApplicationRunner {

    private final CurrencyService currencyService;

    @Override
    public void run(ApplicationArguments args) {
        currencyService.refreshCurrency();
    }

}
