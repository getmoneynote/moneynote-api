package cn.biq.mn.book.tpl;

import cn.biq.mn.base.BaseDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter @Setter
public class BookTemplate extends BaseDetails {

    private String name;
    private String notes;

    private List<TagTemplate> tags;
    private List<CategoryTemplate> categories;
    private List<PayeeTemplate> payees;


}
