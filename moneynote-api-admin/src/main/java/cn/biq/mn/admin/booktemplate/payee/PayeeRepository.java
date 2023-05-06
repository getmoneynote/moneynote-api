package cn.biq.mn.admin.booktemplate.payee;

import cn.biq.mn.admin.booktemplate.book.Book;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;


@Repository
public interface PayeeRepository extends BaseRepository<Payee> {

    boolean existsByBookAndName(Book book, String name);

}
