package cn.biq.mn.admin.rbac.admin;

import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;

@Repository
public interface AdminRepository extends BaseRepository<Admin> {

    Optional<Admin> findOneByUsername(String username);

}
