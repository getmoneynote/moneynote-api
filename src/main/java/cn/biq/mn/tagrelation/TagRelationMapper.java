package cn.biq.mn.tagrelation;

import cn.biq.mn.base.IdAndNameMapper;

public class TagRelationMapper {

    public static TagRelationDetails toDetails(TagRelation entity) {
        if (entity == null) return null;
        var details = new TagRelationDetails();
        details.setId( entity.getId() );
        details.setTag( IdAndNameMapper.toDetails( entity.getTag() ) );
        details.setAmount( entity.getAmount() );
        details.setConvertedAmount( entity.getConvertedAmount() );
        return details;
    }

}
