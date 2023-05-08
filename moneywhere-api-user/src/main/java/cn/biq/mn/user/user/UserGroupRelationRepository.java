package cn.biq.mn.user.user;

import cn.biq.mn.base.base.BaseRepository;
import cn.biq.mn.user.group.Group;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserGroupRelationRepository extends BaseRepository<UserGroupRelation>  {

    Optional<UserGroupRelation> findByGroupAndUser(Group group, User user);

    Integer deleteByGroup(Group group);

}
