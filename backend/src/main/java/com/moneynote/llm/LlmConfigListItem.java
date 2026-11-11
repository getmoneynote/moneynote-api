package com.moneynote.llm;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LlmConfigListItem {

    private Long id;
    private String name;
    private String baseUrl;
    private String model;
    private String maskedApiKey;
    private String notes;
    private Boolean isCurrent;

}
