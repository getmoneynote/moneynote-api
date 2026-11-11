package com.moneynote.exception;

import org.springframework.context.MessageSourceResolvable;

/**
 * 表示 URL/路径级别的主资源缺失时抛出的异常，即客户端请求的目标资源本身不存在
 * （例如 {@code GET /currencies/{id}} 中的 {@code id} 在数据库中查不到，
 * 或存在但不属于当前用户）。
 *
 * <p>统一映射为 404 Not Found。对应消息模板 key：{@code error.resource.not.found}。
 *
 * <p>注意与 {@link ResourceInvalidException} 区分：本异常只用于接口路径中
 * 明确指向的主资源缺失场景；如果缺失的是请求体/查询参数中引用的某个辅助资源
 * （例如查询列表时传入的某个过滤条件 ID 无效），应使用 {@link ResourceInvalidException}
 * （400 Bad Request），而不是本异常，避免 404 语义被滥用到"参数校验失败"的场景上。
 *
 * @param resourceNameKey messages.properties 中资源名词表的 key
 *                         （如 "resource.currency"），而不是解析后的文案，
 *                         内部会包装为 {@link MessageSourceResolvable} 以保证
 *                         跟随请求 Locale 延迟渲染。
 *
 * @see ResourceInvalidException
 * @see ResourceDuplicateException
 * @see ResourceProtectedException
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceNameKey) {
        super("error.resource.not.found", resolvable(resourceNameKey));
    }

}
