package cn.biq.mn.categoryrelation;

import cn.biq.mn.base.IdAndNameMapper;

public class CategoryRelationMapper {

    public static CategoryRelationDetails toDetails(CategoryRelation entity) {
        if (entity == null) return null;
        var details = new CategoryRelationDetails();
        details.setId( entity.getId() );
        details.setCategory( IdAndNameMapper.toDetails( entity.getCategory() ) );
        details.setAmount( entity.getAmount() );
        details.setConvertedAmount( entity.getConvertedAmount() );
        return details;
    }

}
