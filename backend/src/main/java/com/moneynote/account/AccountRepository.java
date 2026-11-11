package com.moneynote.account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.moneynote.base.BaseRepository;

@Repository
public interface AccountRepository extends BaseRepository<Account>, JpaSpecificationExecutor<Account> {

    boolean existsByUserIdAndName(Long userId, String name);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    List<Account> findByUserId(Long userId);

}
