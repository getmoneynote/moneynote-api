package cn.biq.mn.categoryrelation;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.category.Category;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRelationRepository extends BaseRepository<CategoryRelation> {

    boolean existsByCategoryIn(List<Category> categories);

    boolean existsByCategory(Category category);

}
