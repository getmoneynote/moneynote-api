package com.moneynote.reminder;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.moneynote.base.BaseRepository;

@Repository
public interface ReminderRepository extends BaseRepository<Reminder> {

    Optional<Reminder> findByIdAndUserId(Long id, Long userId);

    Page<Reminder> findByUserId(Long userId, Pageable pageable);

    Page<Reminder> findByUserIdAndCompleted(Long userId, Boolean completed, Pageable pageable);

}
