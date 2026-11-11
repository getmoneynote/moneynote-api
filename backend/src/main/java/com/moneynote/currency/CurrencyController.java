package com.moneynote.currency;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.moneynote.response.DataResponse;
import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;

@RestController
@RequestMapping("/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody CurrencyCreateForm form) {
        currencyService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody CurrencyUpdateForm form) {
        currencyService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        currencyService.delete(id);
        return new SimpleResponse();
    }

    @GetMapping
    public SimpleResponse list(CurrencyQueryForm form, Pageable page) {
        return new PageResponse<>(currencyService.list(form, page));
    }

    @GetMapping("/options")
    public SimpleResponse options() {
        return new DataResponse<>(currencyService.listOptions());
    }

}
