package cn.biq.mn.user.payee;

import cn.biq.mn.user.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameEntity;

@Entity
@Table(name = "t_user_payee")
@Getter
@Setter
public class Payee extends IdAndNameEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Book book;

    @Column(length = 4096)
    private String notes;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    @NotNull
    private Boolean canExpense; //是否可支出

    @Column(nullable = false)
    @NotNull
    private Boolean canIncome; //是否可收入

}
