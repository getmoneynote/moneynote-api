package cn.biq.mn.user.template.payee;

import cn.biq.mn.user.template.book.BookTemplate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_admin_book_template_payee")
@Getter @Setter
public class PayeeTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private BookTemplate book;

    private String name;

    private String notes;

    private Boolean canExpense;

    private Boolean canIncome;


}
