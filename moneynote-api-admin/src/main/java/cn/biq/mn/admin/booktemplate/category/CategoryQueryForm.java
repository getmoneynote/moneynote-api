package cn.biq.mn.admin.booktemplate.category;

import cn.biq.mn.admin.entity.admin.QCategory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryQueryForm {

    @NotNull
    private Integer book;
    @NotNull
    private CategoryType type;
    private String name;

    public Predicate buildPredicate() {
        QCategory category = QCategory.category;
        BooleanBuilder builder = new BooleanBuilder(category.book.id.eq(book));
        builder.and(category.type.eq(type));
        if (name != null) {
            builder.and(category.name.contains(name));
        }
        return builder;
    }

}
