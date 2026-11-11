package com.moneynote.llm;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneynote.response.DataResponse;
import com.moneynote.response.SimpleResponse;

@RestController
@RequestMapping("/llm-configs")
@RequiredArgsConstructor
public class LlmConfigController {

    private final LlmConfigService llmConfigService;

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody LlmConfigCreateForm form) {
        llmConfigService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody LlmConfigUpdateForm form) {
        llmConfigService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        llmConfigService.delete(id);
        return new SimpleResponse();
    }

    @GetMapping
    public SimpleResponse list() {
        return new DataResponse<>(llmConfigService.list());
    }

    @PatchMapping("/{id}/activate")
    public SimpleResponse activate(@PathVariable("id") Long id) {
        return new DataResponse<>(llmConfigService.activate(id));
    }

    @PostMapping("/test")
    public SimpleResponse test(@Valid @RequestBody LlmConfigTestForm form) {
        llmConfigService.test(form);
        return new SimpleResponse();
    }

}
