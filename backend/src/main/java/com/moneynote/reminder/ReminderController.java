package com.moneynote.reminder;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;

@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public SimpleResponse list(@Valid ReminderQueryForm form, Pageable page) {
        return new PageResponse<>(reminderService.list(form, page));
    }

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody ReminderCreateForm form) {
        reminderService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody ReminderUpdateForm form) {
        reminderService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        reminderService.delete(id);
        return new SimpleResponse();
    }

}
