package cn.biq.mn.tree;

import cn.biq.mn.base.BaseEntity;
import cn.biq.mn.base.IdAndNameEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TreeEntity<T extends TreeEntity<T>> extends IdAndNameEntity {

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private T parent;

    @Column(nullable = false)
    private Integer level;

}
