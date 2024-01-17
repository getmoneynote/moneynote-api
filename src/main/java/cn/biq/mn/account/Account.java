package cn.biq.mn.account;

import cn.biq.mn.base.IdAndNameEntity;
import cn.biq.mn.validation.*;
import cn.biq.mn.group.Group;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;


@Entity
@Table(name = "t_user_account")
@Getter @Setter
public class Account extends IdAndNameEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @NotNull
    private Group group; // 账簿必须属于某个组

    @Column(nullable = false)
//    @Enumerated(EnumType.ORDINAL)
    @Convert(converter = AccountTypeAttributeConverter.class)
    @NotNull
    private AccountType type; //1活期，2信用，3贷款，4资产

    @Column(length = 1024)
    @NotesField
    private String notes;

    @Column(nullable = false)
    @NotNull
    private Boolean enable;

    @Column(length = 32)
    @AccountNoField
    private String no; //卡号

    @Column(nullable = false) //最多9亿
    @NotNull
    @AmountField
    private BigDecimal balance; // 当前余额

    @Column(nullable = false)
    @NotNull
    private Boolean include;

    @Column(nullable = false)
    @NotNull
    private Boolean canExpense;

    @Column(nullable = false)
    @NotNull
    private Boolean canIncome;

    @Column(nullable = false)
    @NotNull
    private Boolean canTransferFrom;

    @Column(nullable = false)
    @NotNull
    private Boolean canTransferTo;

    @Column(nullable = false, length = 8)
    @NotBlank
    @AccountCurrencyCodeField
    private String currencyCode;

    @AmountField
    private BigDecimal initialBalance; // 期初余额

    @AmountField
    private BigDecimal creditLimit; // 信用额度

    @BillDayField
    private Integer billDay; // 每月多少号是账单日

    @Digits(integer = 4, fraction = 4)
    private BigDecimal apr; // 年化利率(%)

    @Column(name="deleted", columnDefinition="bit(1) default 0")
    private Boolean deleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(getId(), account.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
