package com.moneynote.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 携带业务数据的成功响应，在 {@link SimpleResponse} 基础上增加一个 {@code data} 字段。
 *
 * <p>用于需要向客户端返回具体数据的接口，例如：
 * <ul>
 *   <li>{@code DataResponse<String>}：登录返回 token；</li>
 *   <li>{@code DataResponse<UserSessionVo>}：返回用户会话信息。</li>
 * </ul>
 *
 * <p>构造时 {@code success} 固定为 {@code true}，因为本类型专用于成功返回；
 * 若需在失败时携带数据，应使用其它响应类型。
 *
 * @param <T> data 字段的类型
 */
@Getter @Setter
@NoArgsConstructor
public class DataResponse<T> extends SimpleResponse {

    private T data;

    public DataResponse(T data) {
        setSuccess(true);
        this.data = data;
    }

}
