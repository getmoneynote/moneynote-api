package cn.biq.mn.user.categoryrelation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;
import cn.biq.mn.base.validation.NotZero;

import java.math.BigDecimal;
import java.util.Objects;

@Getter @Setter
public class CategoryRelationForm {

    @NotNull
    private Integer categoryId;

    @NotNull
    @AmountField
    @NotZero
    private BigDecimal amount;

    @AmountField
    @NotZero
    private BigDecimal convertedAmount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryRelationForm that = (CategoryRelationForm) o;
        return categoryId.equals(that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    public boolean equals(CategoryRelation categoryRelation) {
        if (!categoryRelation.getCategory().getId().equals(categoryId)) {
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
