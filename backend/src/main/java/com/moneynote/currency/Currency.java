package com.moneynote.currency;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.base.BaseEntity;
import com.moneynote.user.User;

@Entity
@Table(
        name = "t_currency",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "code"})
)
@Getter @Setter
public class Currency extends BaseEntity {

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * ISO 4217 三位货币代码
     *
     * CNY
     * USD
     * VND
     */
    @Column(length = 3, nullable = false)
    private String code;

    /**
     * 币种名称
     *
     * 人民币
     * 美元
     * 越南盾
     */
    @Column(length = 16, nullable = false)
    private String name;

    /**
     * 符号
     *
     * ¥
     * $
     * ₫
     */
    @Column(length = 16, nullable = false)
    private String symbol;

    /**
     * 最小货币单位的小数位
     *
     * CNY 2
     * USD 2
     * VND 0
     */
    @Column(nullable = false)
    private Integer decimalPlaces;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal usdRate; // 基于USD的汇率

}
