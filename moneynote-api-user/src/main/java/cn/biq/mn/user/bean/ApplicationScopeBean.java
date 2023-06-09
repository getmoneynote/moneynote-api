package cn.biq.mn.user.bean;

import cn.biq.mn.user.currency.CurrencyDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.List;

@Component
@ApplicationScope
@Getter
@Setter
public class ApplicationScopeBean {

    private List<CurrencyDetails> currencyDetailsList;

}
