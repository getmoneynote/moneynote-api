package cn.biq.mn.base.tree;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameEntity;

@MappedSuperclass
@Getter
@Setter
public abstract class TreeEntity extends IdAndNameEntity {

    @Column(nullable = false)
    private Integer level;
    
    public abstract TreeEntity getParent();

}
