package cn.biq.mn.admin.repository.admin;

import cn.biq.mn.admin.entity.admin.Book;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

@Repository
public interface BookRepository extends BaseRepository<Book> {

    boolean existsByName(String name);

}
