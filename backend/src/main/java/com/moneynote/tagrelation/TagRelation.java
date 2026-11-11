package com.moneynote.tagrelation;

import com.moneynote.moneyflow.MoneyFlow;
import com.moneynote.base.BaseEntity;
import com.moneynote.tag.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_tag_relation")
@Getter @Setter
public class TagRelation extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tag_id",
            nullable = false
    )
    private Tag tag;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "money_flow_id",
            nullable = false
    )
    private MoneyFlow moneyFlow;

}
