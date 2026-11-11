package com.moneynote.exception;

/**
 * 表示资源被其他数据引用、无法执行当前操作（通常是删除）时抛出的异常。
 *
 * <p>与 {@link ResourceProtectedException} 的区别：保护异常是"业务规则不允许"
 * （如系统内置币种不可删除），引用异常是"数据上有依赖"（如被账本/账户引用）。
 *
 * <p>建议映射为 409 Conflict：请求语义正确，但因资源状态冲突（被引用）无法执行。
 *
 * @param resourceNameKey 被操作的资源名词 key（如 "resource.currency"）
 */
public class ResourceReferencedException extends BusinessException {

    public ResourceReferencedException(String resourceNameKey) {
        super("error.resource.referenced", resolvable(resourceNameKey));
    }
}
