package cn.biq.mn.bean;

import cn.biq.mn.book.tpl.BookTemplate;
import cn.biq.mn.currency.CurrencyDetails;
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

    private BookTemplate[] bookTplList;

}
