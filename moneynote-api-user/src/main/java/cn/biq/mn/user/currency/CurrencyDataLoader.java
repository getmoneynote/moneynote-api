package cn.biq.mn.user.currency;

import cn.biq.mn.base.utils.WebUtils;
import cn.biq.mn.user.bean.ApplicationScopeBean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CurrencyDataLoader implements ApplicationRunner {

    private final ApplicationScopeBean applicationScopeBean;

    @Override
    public void run(ApplicationArguments args) {
        ArrayList<CurrencyDetails> currencyDetailsList = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource("currency.json");
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> lists = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            for (Map<String, Object> item : lists) {
                CurrencyDetails currencyDetails = new CurrencyDetails();
                currencyDetails.setId((Integer) item.get("id"));
                currencyDetails.setName(item.get("name").toString());
                currencyDetails.setRate(((Number) item.get("rate")).doubleValue());
                currencyDetailsList.add(currencyDetails);
            }
        } catch (Exception e) {
//            currencyDetailsList.add(new CurrencyDetails(1, "USD", 1.0));
//            currencyDetailsList.add(new CurrencyDetails(2, "CNY", 7.1));
        }
        applicationScopeBean.setCurrencyDetailsList(currencyDetailsList);
    }

}
