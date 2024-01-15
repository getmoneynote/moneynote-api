package cn.biq.mn.category;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.book.Book;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findOneByBookAndId(Book book, Integer id);

    long countByBook(Book book);


    List<Category> findAllByBookAndType(Book book, Integer type);

//    @Modifying
//    @Query("delete from Category where book = :book")
    void deleteByBook(Book book);

    @Modifying
    @Transactional
    @Query("UPDATE Category SET parent = NULL where parent = :category")
    void unChildren(Category category);

}
