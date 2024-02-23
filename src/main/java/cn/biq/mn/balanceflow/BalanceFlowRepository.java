package cn.biq.mn.balanceflow;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.account.Account;
import cn.biq.mn.book.Book;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BalanceFlowRepository extends BaseRepository<BalanceFlow>  {

    boolean existsByAccountOrTo(Account account, Account to);

    boolean existsByBook(Book book);

    long countByCreatorAndInsertAtBetween(User creator, long start, long end);

    boolean existsByPayee(Payee payee);

    List<BalanceFlow> findAllByBookOrderByCreateTimeDesc(Book book);

}
