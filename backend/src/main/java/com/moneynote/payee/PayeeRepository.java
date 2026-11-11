package com.moneynote.payee;

import com.moneynote.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayeeRepository extends BaseRepository<Payee>, JpaSpecificationExecutor<Payee> {

    boolean existsByBookIdAndName(Long bookId, String name);

    Optional<Payee> findByIdAndBookId(Long id, Long bookId);
}