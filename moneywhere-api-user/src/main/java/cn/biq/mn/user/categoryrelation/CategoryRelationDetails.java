package cn.biq.mn.user.categoryrelation;

import cn.biq.mn.user.category.CategoryDetails;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;

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
