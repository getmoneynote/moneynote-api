package cn.biq.mn.base;

import cn.biq.mn.validation.NotesField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@MappedSuperclass
@Getter
@Setter
public abstract class NameNotesEnableEntity extends IdAndNameEntity {

    @Column(length = 1024)
    @NotesField
    private String notes;

    @Column(nullable = false)
    @NotNull
    private Boolean enable = true;

}
