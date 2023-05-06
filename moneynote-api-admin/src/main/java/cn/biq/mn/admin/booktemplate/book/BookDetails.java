package cn.biq.mn.admin.booktemplate.book;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;


@Getter @Setter
public class BookDetails extends BaseDetails {

    private String name;
    private String notes;
    private String previewUrl;
    private Boolean visible;

}
