package cn.biq.mn.admin.entity.admin;


import cn.biq.mn.admin.entity.admin.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_admin_book_template_payee")
@Getter @Setter
public class Payee extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Book book;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 4096)
    private String notes;

    @Column(nullable = false)
    private Boolean canExpense; //是否可支出

    @Column(nullable = false)
    private Boolean canIncome; //是否可收入

}
