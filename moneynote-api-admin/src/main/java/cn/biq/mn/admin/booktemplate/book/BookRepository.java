package cn.biq.mn.admin.booktemplate.book;

import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

@Repository
public interface BookRepository extends BaseRepository<Book> {

    boolean existsByName(String name);

}
