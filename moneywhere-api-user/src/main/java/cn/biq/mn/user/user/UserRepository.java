package cn.biq.mn.user.user;

import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User>  {

    Optional<User> findOneByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findOneByUnionId(String unionId);

}
