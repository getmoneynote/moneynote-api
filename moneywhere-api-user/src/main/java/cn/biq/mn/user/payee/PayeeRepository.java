package cn.biq.mn.user.payee;

import cn.biq.mn.user.book.Book;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;


@Repository
public interface PayeeRepository extends BaseRepository<Payee>  {

    boolean existsByBookAndName(Book book, String name);

    long countByBook(Book book);

    Optional<Payee> findOneByBookAndId(Book book, Integer id);

    void deleteByBook(Book book);

}
