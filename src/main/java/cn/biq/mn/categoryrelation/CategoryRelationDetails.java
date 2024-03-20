package cn.biq.mn.categoryrelation;

import cn.biq.mn.base.BaseDetails;
import cn.biq.mn.base.IdAndNameDetails;
import cn.biq.mn.category.CategoryDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CategoryRelationDetails extends BaseDetails {

    private IdAndNameDetails category;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

    public Integer getCategoryId() {
        return category.getId();
    }

    public String getCategoryName() {
        return category.getName();
    }

}
