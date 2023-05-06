package cn.biq.mn.user.account;

import cn.biq.mn.user.group.Group;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.List;

@Repository
public interface AccountRepository extends BaseRepository<Account> {

    boolean existsByGroupAndName(Group group, String name);

    int countByGroup(Group group);

    List<Account> findAllByGroupAndTypeAndEnableAndInclude(Group group, AccountType type, Boolean enable, Boolean include);

}
