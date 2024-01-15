package cn.biq.mn.category;

import cn.biq.mn.tree.TreeEntity;
import cn.biq.mn.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Entity
@Table(name = "t_user_category")
@Getter @Setter
public class Category extends TreeEntity<Category> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    @Column(length = 16, nullable = false)
    private String name;

    @Column(length = 1024)
    private String notes;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    private Integer type; // 100是支出分类，200是收入分类

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
