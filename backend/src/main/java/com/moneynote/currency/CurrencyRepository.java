package com.moneynote.currency;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.moneynote.base.BaseRepository;

@Repository
public interface CurrencyRepository extends BaseRepository<Currency> {

    boolean existsByUserIdAndCode(Long userId, String code);

    Optional<Currency> findByIdAndUserId(Long id, Long userId);

    Page<Currency> findByUserId(Long userId, Pageable pageable);

    List<Currency> findByUserId(Long userId, Sort sort);

    List<Currency> findByUserId(Long userId);

}
