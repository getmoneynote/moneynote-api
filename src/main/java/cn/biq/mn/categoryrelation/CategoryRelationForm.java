package cn.biq.mn.categoryrelation;

import cn.biq.mn.validation.AmountField;
import cn.biq.mn.validation.NotZero;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter @Setter
public class CategoryRelationForm {

    @NotNull
    private Integer category;

    @NotNull
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryRelationForm that = (CategoryRelationForm) o;
        return category.equals(that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    public boolean equals(CategoryRelation categoryRelation) {
        if (!categoryRelation.getCategory().getId().equals(category)) {
            return false;
        }
        if (categoryRelation.getAmount().compareTo(amount) != 0) {
            return false;
        }
        if (categoryRelation.getConvertedAmount().compareTo(convertedAmount) != 0) {
            return false;
        }
        return true;
    }

}
