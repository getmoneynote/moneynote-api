package com.moneynote.exception;

/**
 * 表示资源存在但被业务规则保护、禁止执行当前操作时抛出的异常
 * （例如禁止删除系统内置的 USD/CNY 货币，或禁止修改 USD 的汇率）。
 *
 * <p>与 {@link ResourceNotFoundException} 不同的是：本异常的构造函数直接接收
 * 完整的 {@code messageKey}（如 "error.currency.remove.protected"），
 * 而不是"资源名 + 统一模板"的组合方式，因为每种保护场景通常需要一句
 * 更具体、更有针对性的提示文案，不适合套用统一模板。
 *
 * <p>建议映射为 403 Forbidden：请求的资源是明确存在的，只是业务规则不允许
 * 对其执行该操作，语义上区别于"资源不存在"（404）和"参数无效"（400）。
 *
 * @param messageKey 完整的国际化消息 key，对应具体的保护场景提示文案
 *
 * @see ResourceNotFoundException
 * @see ResourceInvalidException
 * @see ResourceDuplicateException
 */
public class ResourceProtectedException extends BusinessException {

    public ResourceProtectedException(String messageKey) {
        super(messageKey);
    }

}
