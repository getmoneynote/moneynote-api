package com.moneynote.tag;

import com.moneynote.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends BaseRepository<Tag>, JpaSpecificationExecutor<Tag> {

    boolean existsByBookIdAndName(Long bookId, String name);

    Optional<Tag> findByIdAndBookId(Long id, Long bookId);
}