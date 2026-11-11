package com.moneynote.tag;

import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public SimpleResponse list(@Valid TagQueryForm form, Pageable page) {
        Page<TagListItem> result = tagService.list(form, page);
        return new PageResponse<>(result);
    }

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody TagCreateForm form) {
        tagService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody TagUpdateForm form) {
        tagService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        tagService.delete(id);
        return new SimpleResponse();
    }
}
