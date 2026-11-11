package com.moneynote.reminder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReminderListItem> list(ReminderQueryForm form, Pageable page) {
        Long userId = CurrentUserHolder.get();
        Pageable sortedPage = page.getSort().isSorted()
                ? page
                : PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by(Sort.Order.asc("executeDate"), Sort.Order.asc("id")));
        Page<Reminder> entityPage = form.getCompleted() != null
                ? reminderRepository.findByUserIdAndCompleted(userId, form.getCompleted(), sortedPage)
                : reminderRepository.findByUserId(userId, sortedPage);
        return entityPage.map(item -> {
            ReminderListItem listItem = new ReminderListItem();
            listItem.setId(item.getId());
            listItem.setTitle(item.getTitle());
            listItem.setNotes(item.getNotes());
            listItem.setExecuteDate(item.getExecuteDate());
            listItem.setCompleted(item.getCompleted());
            return listItem;
        });
    }

    @Transactional
    public void create(ReminderCreateForm form) {
        Long userId = CurrentUserHolder.get();
        Reminder entity = new Reminder();
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setTitle(form.getTitle());
        entity.setNotes(form.getNotes());
        entity.setExecuteDate(form.getExecuteDate());
        entity.setCompleted(false);
        reminderRepository.save(entity);
    }

    @Transactional
    public void update(Long id, ReminderUpdateForm form) {
        Long userId = CurrentUserHolder.get();
        Reminder entity = reminderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.reminder"));
        if (form.getTitle() != null) {
            entity.setTitle(form.getTitle());
        }
        if (form.getNotes() != null) {
            entity.setNotes(form.getNotes());
        }
        if (form.getExecuteDate() != null) {
            entity.setExecuteDate(form.getExecuteDate());
        }
        if (form.getCompleted() != null) {
            entity.setCompleted(form.getCompleted());
        }
    }

    @Transactional
    public void delete(Long id) {
        Long userId = CurrentUserHolder.get();
        reminderRepository.findByIdAndUserId(id, userId).ifPresent(reminderRepository::delete);
    }

}
