package cn.biq.mn.user;

import cn.biq.mn.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User>  {

    Optional<User> findOneByUsername(String username);

    boolean existsByUsername(String username);

//    Optional<User> findOneByUnionId(String unionId);

}
