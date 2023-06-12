package cn.biq.mn.admin.repository.admin;

import cn.biq.mn.admin.booktemplate.category.CategoryType;
import cn.biq.mn.admin.entity.admin.Book;
import cn.biq.mn.admin.entity.admin.Category;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;


@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findOneByBookAndId(Book book, Integer id);

    boolean existsByBookAndParentAndName(Book book, Category parent, String name);

    boolean existsByBookAndParentAndTypeAndName(Book book, Category parent, CategoryType type, String name);

}
