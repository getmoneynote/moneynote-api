package cn.biq.mn.user.group;

import cn.biq.mn.user.user.User;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;


@Repository
public interface GroupRepository extends BaseRepository<Group>  {

    int countByCreator(User creator);

}
