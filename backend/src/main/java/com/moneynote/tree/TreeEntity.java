package com.moneynote.tree;

import com.moneynote.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class TreeEntity<T extends TreeEntity<T>> extends BaseEntity {

    @Column(length = 64, nullable = false)
    private String name;

    @ManyToOne(
            optional = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            nullable = true,
            name = "p_id"
    )
    private T parent;

}
