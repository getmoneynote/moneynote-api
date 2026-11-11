package com.moneynote.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageSendForm {

    @NotBlank
    @Size(max = 4096)
    private String content;

}
