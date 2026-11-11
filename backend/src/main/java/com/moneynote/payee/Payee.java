package com.moneynote.payee;

import com.moneynote.base.BaseEntity;
import com.moneynote.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_payee")
@Getter @Setter
public class Payee extends BaseEntity {

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

    @Column(length = 1024)
    private String notes;

    @Column(nullable = false)
    private Boolean canExpense; //是否可支出

    @Column(nullable = false)
    private Boolean canIncome; //是否可收入

}
