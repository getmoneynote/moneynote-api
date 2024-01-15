package cn.biq.mn.categoryrelation;

import cn.biq.mn.category.CategoryMapper;

public class CategoryRelationMapper {

    public static CategoryRelationDetails toDetails(CategoryRelation entity) {
        if (entity == null) return null;
        var details = new CategoryRelationDetails();
        details.setId( entity.getId() );
        details.setCategory( CategoryMapper.toDetails( entity.getCategory() ) );
        details.setAmount( entity.getAmount() );
        details.setConvertedAmount( entity.getConvertedAmount() );
        return details;
    }

}
