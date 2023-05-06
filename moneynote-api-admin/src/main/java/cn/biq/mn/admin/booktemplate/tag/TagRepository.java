package cn.biq.mn.admin.booktemplate.tag;

import org.springframework.stereotype.Repository;
import cn.biq.mn.admin.booktemplate.book.Book;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;


@Repository
public interface TagRepository extends BaseRepository<Tag> {

    Optional<Tag> findOneByBookAndId(Book book, Integer id);

    boolean existsByBookAndParentAndName(Book book, Tag parent, String name);

}
