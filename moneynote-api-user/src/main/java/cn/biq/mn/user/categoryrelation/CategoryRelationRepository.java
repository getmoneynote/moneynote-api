package cn.biq.mn.user.categoryrelation;

import cn.biq.mn.user.category.Category;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.List;


@Repository
public interface CategoryRelationRepository extends BaseRepository<CategoryRelation> {

    boolean existsByCategoryIn(List<Category> categories);

    boolean existsByCategory(Category category);

}
