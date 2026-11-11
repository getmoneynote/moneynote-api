package com.moneynote.book;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.moneynote.base.BaseRepository;

@Repository
public interface BookRepository extends BaseRepository<Book> {

    boolean existsByUserIdAndName(Long userId, String name);

    boolean existsByIdAndUserId(Long id, Long userId);

    Optional<Book> findByIdAndUserId(Long id, Long userId);

    Page<Book> findByUserId(Long userId, Pageable pageable);

}
