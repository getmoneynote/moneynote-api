package cn.biq.mn.admin.repository.admin;

import cn.biq.mn.admin.entity.admin.Admin;
import org.springframework.stereotype.Repository;
import cn.biq.mn.base.base.BaseRepository;

import java.util.Optional;

@Repository
public interface AdminRepository extends BaseRepository<Admin> {

    Optional<Admin> findOneByUsername(String username);

}
