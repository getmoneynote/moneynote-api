package com.moneynote.book;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.base.BaseEntity;
import com.moneynote.currency.Currency;
import com.moneynote.user.User;

@Entity
@Table(name = "t_book")
@Getter @Setter
public class Book extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user; // 账簿必须属于某个用户

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 1024, nullable = true)
    private String notes;

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "currency_id",
            nullable = false
    )
    private Currency currency;//账本的币种

}
