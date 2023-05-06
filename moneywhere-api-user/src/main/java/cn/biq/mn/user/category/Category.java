package cn.biq.mn.user.category;

import cn.biq.mn.user.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.tree.TreeEntity;

import java.util.Objects;


@Entity
@Table(name = "t_user_category")
@Getter @Setter
public class Category extends TreeEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    @Column(length = 16, nullable = false)
    private String name;

    @Column(length = 1024)
    private String notes;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private CategoryType type;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Category parent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(getId(), category.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }


}
