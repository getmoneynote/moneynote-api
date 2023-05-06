package cn.biq.mn.user.template.book;

import cn.biq.mn.user.template.tag.TagTemplate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;
import cn.biq.mn.user.template.category.CategoryTemplate;
import cn.biq.mn.user.template.payee.PayeeTemplate;

import java.util.List;

@Entity
@Table(name = "t_admin_book_template")
@Getter @Setter
public class BookTemplate extends BaseEntity {

    private String name;

    private String notes;

    private String previewUrl;

    private Boolean visible;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<CategoryTemplate> categories;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<PayeeTemplate> payees;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<TagTemplate> tags;

}
