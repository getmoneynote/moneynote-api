package cn.biq.mn.admin.entity.admin;

import cn.biq.mn.admin.booktemplate.category.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.tree.TreeEntity;

@Entity
@Table(name = "t_admin_book_template_category")
@Getter @Setter
public class Category extends TreeEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 4096)
    private String notes;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private CategoryType type;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Category parent;

}
