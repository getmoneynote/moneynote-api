package com.moneynote.book;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody BookCreateForm form) {
        bookService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody BookUpdateForm form) {
        bookService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        bookService.delete(id);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}/setDefault")
    public SimpleResponse setDefault(@PathVariable("id") Long id) {
        bookService.setDefault(id);
        return new SimpleResponse();
    }

    @GetMapping
    public SimpleResponse list(Pageable page) {
        return new PageResponse<>(bookService.list(page));
    }

}
