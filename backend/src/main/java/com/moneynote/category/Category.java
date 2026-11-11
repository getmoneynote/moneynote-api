package com.moneynote.category;

import com.moneynote.book.Book;
import com.moneynote.tree.TreeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_category")
@Getter @Setter
public class Category extends TreeEntity<Category> {

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    @Column(length = 4096)
    private String notes;

    private Integer type;

}
