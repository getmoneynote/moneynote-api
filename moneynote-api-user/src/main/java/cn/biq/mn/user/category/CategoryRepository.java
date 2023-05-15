package cn.biq.mn.user.category;

import cn.biq.mn.user.book.Book;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import cn.biq.mn.base.base.BaseRepository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findOneByBookAndId(Book book, Integer id);

    long countByBook(Book book);

    boolean existsByBookAndParentAndTypeAndName(Book book, Category parent, CategoryType type, String name);

    List<Category> findAllByBookAndType(Book book, CategoryType type);

//    @Modifying
//    @Query("delete from Category where book = :book")
    void deleteByBook(Book book);

    @Modifying
    @Transactional
    @Query("UPDATE Category SET parent = NULL where parent = :category")
    void unChildren(Category category);

}
