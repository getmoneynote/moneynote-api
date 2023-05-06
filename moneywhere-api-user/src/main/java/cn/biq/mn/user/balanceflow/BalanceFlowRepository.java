package cn.biq.mn.user.balanceflow;

import cn.biq.mn.user.account.Account;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.payee.Payee;
import cn.biq.mn.user.user.User;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;


@Repository
public interface BalanceFlowRepository extends BaseRepository<BalanceFlow>  {

    boolean existsByAccountOrTo(Account account, Account to);

    boolean existsByBook(Book book);

    long countByCreatorAndInsertAtBetween(User creator, long start, long end);

    boolean existsByPayee(Payee payee);

}
