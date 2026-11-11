package com.moneynote.user;

import com.moneynote.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findOneByUsername(String username);

    boolean existsByUsername(String username);

}
