package cn.biq.mn.base.base;

import cn.biq.mn.base.validation.NameField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class IdAndNameEntity extends BaseEntity {

    @Column(length = 64, nullable = false)
    @NameField
    private String name;

}
