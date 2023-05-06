package cn.biq.mn.user.template.category;


import cn.biq.mn.user.category.CategoryType;
import cn.biq.mn.user.template.book.BookTemplate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_admin_book_template_category")
@Getter @Setter
public class CategoryTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private BookTemplate book;

    @Enumerated(EnumType.ORDINAL)
    private CategoryType type;

    private String name;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    private CategoryTemplate parent;

    private Integer level;

}
