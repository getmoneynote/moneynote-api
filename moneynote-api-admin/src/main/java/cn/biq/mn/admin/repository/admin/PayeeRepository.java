package cn.biq.mn.admin.repository.admin;

import cn.biq.mn.admin.entity.admin.Book;
import cn.biq.mn.admin.entity.admin.Payee;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;


@Repository
public interface PayeeRepository extends BaseRepository<Payee> {

    boolean existsByBookAndName(Book book, String name);

}
