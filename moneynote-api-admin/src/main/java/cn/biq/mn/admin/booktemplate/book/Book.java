package cn.biq.mn.admin.booktemplate.book;

import cn.biq.mn.admin.booktemplate.tag.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.admin.booktemplate.category.Category;
import cn.biq.mn.admin.booktemplate.payee.Payee;
import cn.biq.mn.base.base.BaseEntity;

import java.util.List;

@Entity
@Table(name = "t_admin_book_template")
@Getter @Setter
public class Book extends BaseEntity {

    @Column(length = 64, nullable = false, unique = true)
    private String name;

    @Column(length = 4096)
    private String notes;

    @Column(length = 256)
    private String previewUrl;

    @Column(nullable = false)
    private Boolean visible;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Category> categories;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Payee> payees;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Tag> tags;

}
