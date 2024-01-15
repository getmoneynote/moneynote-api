package cn.biq.mn.categoryrelation;

import cn.biq.mn.base.BaseDetails;
import cn.biq.mn.category.CategoryDetails;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CategoryRelationDetails extends BaseDetails {

    private CategoryDetails category;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

    public Integer getCategoryId() {
        return category.getId();
    }

    public String getCategoryName() {
        return category.getName();
    }

}
