package cn.biq.mn.user.tagrelation;

import cn.biq.mn.user.tag.TagDetails;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;

import java.math.BigDecimal;

@Getter
@Setter
public class TagRelationDetails extends BaseDetails {

    private TagDetails tag;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

    public Integer getTagId() {
        return tag.getId();
    }

    public String getTagName() {
        return tag.getName();
    }

}
