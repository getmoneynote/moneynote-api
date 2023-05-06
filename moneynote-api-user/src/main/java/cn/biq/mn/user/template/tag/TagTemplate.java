package cn.biq.mn.user.template.tag;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;
import cn.biq.mn.user.template.book.BookTemplate;

@Entity
@Table(name = "t_admin_book_template_tag")
@Getter @Setter
public class TagTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private BookTemplate book;

    private String name;

    private String notes;

    private Boolean canExpense;

    private Boolean canIncome;

    private Boolean canTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    private TagTemplate parent;

    private Integer level;

}
