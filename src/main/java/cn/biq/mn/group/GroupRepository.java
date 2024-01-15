package cn.biq.mn.group;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.user.User;
import org.springframework.stereotype.Repository;


@Repository
public interface GroupRepository extends BaseRepository<Group>  {

    int countByCreator(User creator);

}
