package cn.biq.mn.admin.booktemplate.category;

import cn.biq.mn.admin.booktemplate.book.Book;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;


@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findOneByBookAndId(Book book, Integer id);

    boolean existsByBookAndParentAndName(Book book, Category parent, String name);

    boolean existsByBookAndParentAndTypeAndName(Book book, Category parent, CategoryType type, String name);

}
