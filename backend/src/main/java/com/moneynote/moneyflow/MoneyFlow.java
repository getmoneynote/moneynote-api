package com.moneynote.moneyflow;

import com.moneynote.account.Account;
import com.moneynote.base.BaseEntity;
import com.moneynote.book.Book;
import com.moneynote.category.Category;
import com.moneynote.payee.Payee;
import com.moneynote.tagrelation.TagRelation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_money_flow")
@Getter @Setter
public class MoneyFlow extends BaseEntity {

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    // 流水类型：1=支出，2=收入，3=转账（转账时 to 为转入账户）；4=余额调整（AccountService.adjustBalance 内部写入，不走本模块 create）
    @Column(nullable = false)
    private Integer type;

    // 原币金额：有账户时与账户币种一致，无账户时按账本币种计量
    @Column(nullable = false)
    private Long amount;

    // 换算金额（最小货币单位，用户输入）：支出/收入为账本币种金额，转账为转入账户到账金额；币种一致时 = amount
    @Column(nullable = false)
    private Long convertedAmount;

    // 支出/收入可空（空 = 未归属/待核对的导入账单）；转账必填，由 Service 层校验。
    @ManyToOne(
            optional = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "account_id",
            nullable = true
    )
    private Account account;

    /**
     * Unix 时间戳，单位：毫秒。
     */
    @Column(nullable = false)
    private Long createTime;

    @Column(length = 32)
    private String title;

    @Column(length = 1024)
    private String notes; //备注

    // 分类仅支出/收入必填，转账(type=3)可为空
    @ManyToOne(
            optional = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "category_id",
            nullable = true
    )
    private Category category;

    @OneToMany(mappedBy = "moneyFlow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TagRelation> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            nullable = true,
            name = "to_account_id"
    )
    private Account to;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            nullable = true,
            name = "payee_id"
    )
    private Payee payee;

    @Column(nullable = false)
    private Boolean confirm; //账单是否确认了，确认了才会执行账户的余额操作

    @Column(nullable = false)
    private Boolean include; //账单是否计入统计数据


}
