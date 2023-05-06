package cn.biq.mn.user.currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;
import cn.biq.mn.base.base.IdAndNameEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "t_admin_currency")
@Getter @Setter
public class Currency extends IdAndNameEntity {

    @Column(length = 128, nullable = false)
    @NotNull
    private String description;

    @Column(nullable = false)
    @NotNull
    @AmountField
    private BigDecimal rate;

    @Column(nullable = false)
    @NotNull
    private Boolean enable;

}
