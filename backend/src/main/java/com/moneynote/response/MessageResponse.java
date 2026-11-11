package com.moneynote.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 携带提示文案的响应，在 {@link SimpleResponse} 基础上增加一个 {@code message} 字段。
 *
 * <p>多用于业务失败时向客户端说明失败原因（如"用户名或密码错误"），
 * 由异常处理器统一构造：{@code success = false} + 经 {@code MessageSource}
 * 按当前 Locale 国际化后的消息文案。
 *
 * <p>子类：{@link ValidationErrorResponse} 在此基础上追加字段级校验明细。
 */
@Getter @Setter
@NoArgsConstructor
public class MessageResponse extends SimpleResponse {

    private String message;

    public MessageResponse(boolean success, String message) {
        setSuccess(success);
        this.message = message;
    }

}
