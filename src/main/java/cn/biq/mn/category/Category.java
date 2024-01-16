package cn.biq.mn.category;

import cn.biq.mn.tree.TreeEntity;
import cn.biq.mn.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "t_user_category")
@Getter
@Setter
public class Category extends TreeEntity<Category> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    @Column(length = 16, nullable = false)
    private String name;

    @Column(length = 4096)
    private String notes;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    private Integer type; // 100是支出分类，200是收入分类

    @Column(name="ranking")
    private Integer sort;

}
