package com.moneynote.exception;

/**
 * 表示已认证用户对当前资源没有执行权限时抛出的异常（建议映射为 403 Forbidden），
 * 与"未认证/登录失效"（401，见 {@link UnauthorizedException}）语义区分。
 *
 * <p>构造函数直接接收完整的 {@code messageKey}（如 "error.auth.forbidden"），
 * 不涉及资源名词的包装，由全局异常处理器统一渲染。
 *
 * @param messageKey 完整的国际化消息 key，对应无权限提示文案
 *
 * @see UnauthorizedException
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String messageKey) {
        super(messageKey);
    }

}
