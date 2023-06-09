package cn.biq.mn.user.currency;

import cn.biq.mn.base.utils.WebUtils;
import cn.biq.mn.user.bean.ApplicationScopeBean;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CurrencyDataLoader implements ApplicationRunner {

    @Value("${user.api.base.url}")
    private String userApiBaseUrl;
    private final WebUtils webUtils;
    private final ApplicationScopeBean applicationScopeBean;

    @Override
    public void run(ApplicationArguments args) {
        HashMap<String, Object> baseUserMap =  webUtils.get(userApiBaseUrl + "/currencies/all");
        ArrayList<Map<String, Object>> lists = (ArrayList<Map<String, Object>>) baseUserMap.get("data");
        ArrayList<CurrencyDetails> currencyDetailsList = new ArrayList<>();
        for (Map<String, Object> item : lists) {
            CurrencyDetails currencyDetails = new CurrencyDetails();
            currencyDetails.setId((Integer) item.get("id"));
            currencyDetails.setName(item.get("name").toString());
            currencyDetails.setRate((Double) item.get("rate"));
            currencyDetailsList.add(currencyDetails);
        }
        applicationScopeBean.setCurrencyDetailsList(currencyDetailsList);
    }

}
