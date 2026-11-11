package com.moneynote.tag;

import com.moneynote.base.BaseEntity;
import com.moneynote.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_tag")
@Getter @Setter
public class Tag extends BaseEntity {

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 4096)
    private String notes;

    @Column(nullable = false)
    private Boolean canExpense;

    @Column(nullable = false)
    private Boolean canIncome;

    @Column(nullable = false)
    private Boolean canTransfer;

}
