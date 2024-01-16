package cn.biq.mn.currency;

import cn.biq.mn.bean.ApplicationScopeBean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrencyDataLoader implements ApplicationRunner {

    private final ApplicationScopeBean applicationScopeBean;

    @Override
    public void run(ApplicationArguments args) {
        List<CurrencyDetails> currencyDetailsList = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource("currency.json");
            ObjectMapper objectMapper = new ObjectMapper();
            currencyDetailsList = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() { });
        } catch (Exception e) {
            e.printStackTrace();
            currencyDetailsList.clear();
            currencyDetailsList.add(new CurrencyDetails(1, "USD", 1.0));
            currencyDetailsList.add(new CurrencyDetails(2, "CNY", 7.1));
        }
        applicationScopeBean.setCurrencyDetailsList(currencyDetailsList);
    }

}
