package cn.biq.mn.balanceflow;

import cn.biq.mn.base.BaseEntity;
import cn.biq.mn.validation.AmountField;
import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import cn.biq.mn.account.Account;
import cn.biq.mn.book.Book;
import cn.biq.mn.categoryrelation.CategoryRelation;
import cn.biq.mn.flowfile.FlowFile;
import cn.biq.mn.group.Group;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.tagrelation.TagRelation;
import cn.biq.mn.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_user_balance_flow")
@Getter @Setter
public class BalanceFlow extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Book book;

    @Column(nullable = false)
//    @Enumerated(EnumType.ORDINAL)
    @Convert(converter = FlowTypeAttributeConverter.class)
    @NotNull
    private FlowType type; //1支出，2收入，3转账，4余额调整

    @Column(nullable = false)
    @NotNull
    @AmountField
    private BigDecimal amount; // 金额

    @AmountField
    private BigDecimal convertedAmount; //汇率换算之后的金额

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Account account;

    @Column(nullable = false)
    @NotNull
    @TimeField
    private Long createTime;

    @Column(length = 32)
    @TitleField
    private String title;

    @Column(length = 1024)
    @NotesField
    private String notes; //备注

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private User creator;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Group group;

    @OneToMany(mappedBy = "balanceFlow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TagRelation> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Account to;

    @ManyToOne(fetch = FetchType.LAZY)
    private Payee payee;

    @Column(nullable = false)
    @NotNull
    private Boolean confirm;

    // Transfer和Adjust类型，可以为空。
    private Boolean include;

    @OneToMany(mappedBy = "balanceFlow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @NotEmpty
    private Set<CategoryRelation> categories = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Long insertAt = System.currentTimeMillis();

    @OneToMany(mappedBy = "flow", fetch = FetchType.LAZY)
    private Set<FlowFile> files = new HashSet<>();

}
