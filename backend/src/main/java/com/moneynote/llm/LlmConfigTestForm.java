package com.moneynote.llm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * 测试连接表单：不保存、直接拿入参验证连通性。
 * apiKey 可空——编辑已有配置时留空表示用库存的 key 测，此时必须带 id（校验归属后取库里的 key）。
 */
@Getter @Setter
public class LlmConfigTestForm {

    private Long id;

    @NotBlank
    @Size(max = 256)
    private String baseUrl;

    @Size(max = 256)
    private String apiKey;

    @NotBlank
    @Size(max = 128)
    private String model;

}
