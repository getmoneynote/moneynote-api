package com.moneynote.category;

import com.moneynote.response.DataResponse;
import com.moneynote.response.SimpleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public SimpleResponse tree(@RequestParam Integer type, @RequestParam(required = false) Long bookId) {
        return new DataResponse<>(categoryService.tree(type, bookId));
    }

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody CategoryCreateForm form) {
        categoryService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody CategoryUpdateForm form) {
        categoryService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return new SimpleResponse();
    }
}
