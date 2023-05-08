package cn.biq.mn.user.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameEntity;
import cn.biq.mn.user.account.Account;
import cn.biq.mn.user.category.Category;
import cn.biq.mn.user.group.Group;

@Entity
@Table(name = "t_user_book")
@Getter @Setter
public class Book extends IdAndNameEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @NotNull
    private Group group; // 账簿必须属于某个组

    @Column(length = 1024)
    private String notes;

    @Column(nullable = false)
    private Boolean enable = true;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Account defaultExpenseAccount;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Account defaultIncomeAccount;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Account defaultTransferFromAccount;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Account defaultTransferToAccount;

    @OneToOne(optional = true, fetch = FetchType.EAGER)
    private Category defaultExpenseCategory;

    @OneToOne(optional = true, fetch = FetchType.EAGER)
    private Category defaultIncomeCategory;

    @Column(nullable = false, length = 8)
    @NotNull
    private String defaultCurrencyCode;//默认的币种

    private Long exportAt;

}
