package com.moneynote.llm;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * 所有字段可空：null 或空白字符串都表示不修改该字段。
 * apiKey 尤其如此——编辑弹窗里留空即保留原 key（前端拿到的只是掩码，不能原样写回）。
 * 例外是 notes：null 不修改、空串清空（与 Book.update 同口径，前端编辑弹窗总是回填该字段）。
 */
@Getter @Setter
public class LlmConfigUpdateForm {

    @Size(max = 64)
    private String name;

    @Size(max = 256)
    private String baseUrl;

    @Size(max = 256)
    private String apiKey;

    @Size(max = 128)
    private String model;

    @Size(max = 1024)
    private String notes;

}
