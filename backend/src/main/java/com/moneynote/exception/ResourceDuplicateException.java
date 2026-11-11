package com.moneynote.exception;

import org.springframework.context.MessageSourceResolvable;

/**
 * 表示新增（或更新为某个已存在值）时，资源发生唯一性冲突时抛出的异常
 * （例如新增货币时 {@code code} 已存在，如 {@link com.moneynote.currency.CurrencyService#create}
 * 中对 {@code (userId, code)} 的重复校验）。
 *
 * <p>提供两个重载，分别对应"是否需要在提示文案中指出具体的冲突值"：
 * <ul>
 *   <li>{@link #ResourceDuplicateException(String)}：只提示"某类资源重复"，
 *       不暴露具体冲突的字段值，对应消息模板 "error.resource.duplicate"，
 *       占位符仅替换资源名。</li>
 *   <li>{@link #ResourceDuplicateException(String, String)}：额外携带
 *       冲突的具体字段值（如重复的货币代码），对应更详细的消息模板
 *       "error.resource.duplicate.field"，同时替换资源名和字段值两个占位符，
 *       便于用户直接定位是哪个值冲突了。</li>
 * </ul>
 *
 * <p><b>参数说明：</b>{@code resourceNameKey} 传的是 messages.properties 中
 * 资源名词表的 key（如 "resource.currency"），而不是解析后的文案，
 * 内部会包装为 {@link MessageSourceResolvable} 以保证跟随请求 Locale
 * 延迟渲染；{@code fieldValue} 是用户输入的原始值，直接传递即可。
 *
 * <p>建议映射为 409 Conflict：请求的操作本身合法，但与服务端当前状态
 * （已存在的同名/同值资源）冲突，区别于 400（参数本身无效）和
 * 404（资源不存在）。
 *
 * @see ResourceNotFoundException
 * @see ResourceInvalidException
 * @see ResourceProtectedException
 */
public class ResourceDuplicateException extends BusinessException {

    public ResourceDuplicateException(String resourceNameKey) {
        super("error.resource.duplicate", resolvable(resourceNameKey));
    }

    public ResourceDuplicateException(String resourceNameKey, String fieldValue) {
        super("error.resource.duplicate.field", resolvable(resourceNameKey), fieldValue);
    }

}
