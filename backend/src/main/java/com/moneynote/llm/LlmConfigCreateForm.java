package com.moneynote.llm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LlmConfigCreateForm {

    @NotBlank
    @Size(max = 64)
    private String name;

    @NotBlank
    @Size(max = 256)
    private String baseUrl;

    @NotBlank
    @Size(max = 256)
    private String apiKey;

    @NotBlank
    @Size(max = 128)
    private String model;

    @Size(max = 1024)
    private String notes;

}
