package cn.biq.mn.currency;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ChangeRateForm {

    private String base;
    private BigDecimal rate;

}
