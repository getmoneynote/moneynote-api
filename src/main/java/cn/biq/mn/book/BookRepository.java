package cn.biq.mn.book;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.account.Account;
import cn.biq.mn.category.Category;
import cn.biq.mn.group.Group;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BookRepository extends BaseRepository<Book>  {

    boolean existsByGroupAndName(Group group, String name);

    boolean existsByDefaultExpenseAccount(Account account);

    boolean existsByDefaultIncomeAccount(Account account);

    boolean existsByDefaultTransferFromAccount(Account account);

    boolean existsByDefaultTransferToAccount(Account account);

    boolean existsByDefaultExpenseCategory(Category category);

    boolean existsByDefaultIncomeCategory(Category category);

    int countByGroup(Group group);

    List<Book> findAllByGroup(Group group);

}
