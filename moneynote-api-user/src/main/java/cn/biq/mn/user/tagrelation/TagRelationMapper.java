package cn.biq.mn.user.tagrelation;

import cn.biq.mn.user.tag.TagMapper;

public class TagRelationMapper {

    public static TagRelationDetails toDetails(TagRelation entity) {
        if (entity == null) return null;
        var details = new TagRelationDetails();
        details.setId( entity.getId() );
        details.setTag( TagMapper.toDetails( entity.getTag() ) );
        details.setAmount( entity.getAmount() );
        details.setConvertedAmount( entity.getConvertedAmount() );
        return details;
    }

}
