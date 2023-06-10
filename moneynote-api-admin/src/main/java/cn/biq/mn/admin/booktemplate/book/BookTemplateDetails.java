package cn.biq.mn.admin.booktemplate.book;

import cn.biq.mn.admin.booktemplate.category.CategoryDetails;
import cn.biq.mn.admin.booktemplate.payee.PayeeDetails;
import cn.biq.mn.admin.booktemplate.tag.TagDetails;
import cn.biq.mn.base.base.BaseDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter @Setter
public class BookTemplateDetails extends BaseDetails {

    private String name;
    private String notes;

    private List<TagDetails> tags;
    private List<CategoryDetails> categories;
    private List<PayeeDetails> payees;


}
