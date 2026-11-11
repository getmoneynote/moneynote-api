package com.moneynote.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 所有 API 响应的基类，只携带一个 {@code success} 布尔标志，表示请求是否成功。
 *
 * <p>适合"只需要告知成功/失败、无需返回业务数据"的接口（如注册、删除、设置操作），
 * Controller 直接返回 {@code new SimpleResponse(true)} 即可。
 *
 * <p>子类扩展：
 * <ul>
 *   <li>{@link DataResponse}：携带业务数据；</li>
 *   <li>{@link MessageResponse}：携带提示文案（多用于业务失败）；</li>
 *   <li>{@link PageResponse}：携带分页数据与分页元信息。</li>
 * </ul>
 *
 * <p>{@code success} 默认值为 {@code true}，正常返回路径无需显式指定；
 * 失败路径（异常处理器）统一传入 {@code false}。
 */
@Getter @Setter
@NoArgsConstructor
public class SimpleResponse {

    private boolean success = true;

    public SimpleResponse(boolean success) {
        this.success = success;
    }
}

