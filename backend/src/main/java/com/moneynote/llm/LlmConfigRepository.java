package com.moneynote.llm;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.moneynote.base.BaseRepository;

@Repository
public interface LlmConfigRepository extends BaseRepository<LlmConfig> {

    boolean existsByUserIdAndName(Long userId, String name);

    Optional<LlmConfig> findByIdAndUserId(Long id, Long userId);

    List<LlmConfig> findByUserId(Long userId, Sort sort);

}
