package cn.biq.mn.tree;

import cn.biq.mn.base.IdAndNameEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TreeEntity<T extends TreeEntity<T>> extends IdAndNameEntity {

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private T parent;

    @Column(nullable = false)
    private Integer level;

}
