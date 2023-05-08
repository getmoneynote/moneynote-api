package cn.biq.mn.user.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;
import cn.biq.mn.user.account.Account;
import cn.biq.mn.user.category.Category;
import cn.biq.mn.user.group.Group;

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

}
