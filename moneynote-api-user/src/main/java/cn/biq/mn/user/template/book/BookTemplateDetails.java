package cn.biq.mn.user.template.book;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameDetails;


@Getter @Setter
public class BookTemplateDetails extends IdAndNameDetails {

    private String notes;
    private String previewUrl;

}
