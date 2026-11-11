package com.moneynote.account;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.base.BaseEntity;
import com.moneynote.currency.Currency;
import com.moneynote.user.User;

@Entity
@Table(
        name = "t_account",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"})
)
@Getter @Setter
public class Account extends BaseEntity {

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

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
    private Currency currency;

    /**
     * 账户类型：1-活期 2-信用 3-资产 4-贷款
     */
    @Column(nullable = false)
    private Integer type;

    @Column(length = 32)
    private String no; //卡号

    /**
     * 余额，以"最小货币单位"存储（如人民币是"分"）
     * 例如 10.50 元 存储为 1050
     */
    @Column(nullable = false)
    private Long balance; // 当前余额

    // 是否计入净资产
    @Column(nullable = false)
    private Boolean include;


    @Column(nullable = false)
    private Boolean canExpense;

    @Column(nullable = false)
    private Boolean canIncome;

    @Column(nullable = false)
    private Boolean canTransferFrom;

    @Column(nullable = false)
    private Boolean canTransferTo;

    // 以"最小货币单位"存储（如人民币是"分"）
    private Long creditLimit; // 信用额度

    @Column(nullable = true)
    private Integer billDay; // 每月多少号是账单日

    /**
     * 年化利率(%)。precision/scale 必须显式指定：BigDecimal 不写时 Hibernate 默认
     * scale=2，前端允许 4 位小数会被静默截断（见 docs/后端经验.md）。
     */
    @Column(nullable = true, precision = 7, scale = 4)
    private BigDecimal apr; // 年化利率(%)

    /**
     * 显示排序，数字越小越靠前；null 表示未设置，排在已设置的后面（list/options 接口均按此排序）。
     */
    @Column(nullable = true)
    private Integer displayOrder;

}
