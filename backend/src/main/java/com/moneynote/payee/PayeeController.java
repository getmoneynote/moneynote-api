package com.moneynote.payee;

import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payees")
@RequiredArgsConstructor
public class PayeeController {

    private final PayeeService payeeService;

    @GetMapping
    public SimpleResponse list(@Valid PayeeQueryForm form, Pageable page) {
        Page<PayeeListItem> result = payeeService.list(form, page);
        return new PageResponse<>(result);
    }

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody PayeeCreateForm form) {
        payeeService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody PayeeUpdateForm form) {
        payeeService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        payeeService.delete(id);
        return new SimpleResponse();
    }
}
