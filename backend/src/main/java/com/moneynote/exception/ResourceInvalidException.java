package com.moneynote.exception;

/**
 * 表示请求参数在格式上合法，但其指向的资源不满足业务要求时抛出的异常。
 *
 * <p>典型场景（外键校验场景）：参数引用了一个不存在的资源、一个不属于当前用户的资源、
 * 或一个状态不满足当前操作要求的资源。这与 {@link ResourceNotFoundException}
 * 的区别在于：后者用于 URL 路径中主资源缺失的场景（对应 404），
 * 而本异常用于请求体/查询参数中引用资源无效的场景，责任在客户端传参，
 * 应映射为 400 Bad Request。
 *
 * <p>示例：查询货币列表时，传入的 {@code base}（基准货币 ID）
 * 不存在或不属于当前用户，应抛出本异常，而不是 {@link ResourceNotFoundException}，
 * 因为该接口的主资源是"货币列表"本身，而不是这个 base 参数。
 *
 * @param messageKey 国际化消息 key：前置兜底校验（前端已拦截的资源引用缺失/失效）统一用
 *                   "error.invalid.argument"；可被用户识别并自行纠正的业务规则校验用具体语义 key
 *
 * @see ResourceNotFoundException
 * @see ResourceDuplicateException
 * @see ResourceProtectedException
 */
public class ResourceInvalidException extends BusinessException {

    public ResourceInvalidException(String messageKey) {
        super(messageKey);
    }

}
