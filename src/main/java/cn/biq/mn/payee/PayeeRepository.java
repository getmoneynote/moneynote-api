package cn.biq.mn.payee;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.book.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PayeeRepository extends BaseRepository<Payee>  {

    boolean existsByBookAndName(Book book, String name);

    long countByBook(Book book);

    Optional<Payee> findOneByBookAndId(Book book, Integer id);

    void deleteByBook(Book book);

    List<Payee> findByBookAndEnableAndCanExpense(Book book, Boolean enable, Boolean canExpense);

    List<Payee> findByBookAndEnableAndCanIncome(Book book, Boolean enable, Boolean canIncome);

}
