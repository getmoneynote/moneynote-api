package cn.biq.mn.user.tag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.tree.TreeEntity;
import cn.biq.mn.user.book.Book;


@Entity
@Table(name = "t_user_tag")
@Getter
@Setter
public class Tag extends TreeEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    @Column(length = 4096)
    private String notes;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    private Boolean canExpense;

    @Column(nullable = false)
    private Boolean canIncome;

    @Column(nullable = false)
    private Boolean canTransfer;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Tag parent;

}
