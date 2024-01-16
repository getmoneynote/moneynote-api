package cn.biq.mn.tag;

import cn.biq.mn.tree.TreeEntity;
import cn.biq.mn.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "t_user_tag")
@Getter
@Setter
public class Tag extends TreeEntity<Tag> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;

    @Column(length = 16, nullable = false)
    private String name;

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

    @Column(name="ranking")
    private Integer sort;

}
