package cn.biq.mn.account;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.group.Group;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends BaseRepository<Account> {

    boolean existsByGroupAndName(Group group, String name);

    int countByGroup(Group group);

    List<Account> findAllByGroupAndTypeAndEnableAndInclude(Group group, AccountType type, Boolean enable, Boolean include);

}
