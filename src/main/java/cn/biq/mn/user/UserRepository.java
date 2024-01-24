package cn.biq.mn.user;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.book.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User>  {

    Optional<User> findOneByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByDefaultBook(Book book);

}
