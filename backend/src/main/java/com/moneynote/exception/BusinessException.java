package com.moneynote.exception;

import lombok.Getter;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;


/**
 * 所有业务异常的基类，代表"预期内的业务失败"（如参数校验不通过、资源状态冲突等），
 * 而非编程错误或系统故障。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code message} 字段（继承自 {@link RuntimeException}）存放的是
 *       <b>国际化消息 key</b>，而不是最终展示给用户的文案。真正的文案由
 *       全局异常处理器结合 {@code messageKey} 和 {@link #args} 通过
 *       {@code MessageSource}（或类似机制）在响应返回前渲染生成。</li>
 *   <li>{@link #args} 是消息模板中的占位符参数，按顺序对应 key 对应文案里的
 *       {@code {0}}、{@code {1}} 等占位符。例如 {@code messageKey} 为
 *       "error.resource.not.found"、文案模板为 "{0} 不存在" 时，
 *       {@code args[0]} 即填充到 {0} 处的资源名。
 *       如果某个子类的消息不需要占位符（如 {@link ResourceProtectedException}），
 *       可以不传 args，此时为空数组。</li>
 *   <li>{@link #args} 中的元素分两类：一类是<b>原始数据值</b>（用户输入、ID、数字等），
 *       按原样传递；另一类是<b>资源名词 key</b>（messages.properties 资源名词表中的
 *       词条，如 "resource.currency"），必须通过 {@link #resolvable(String)} 包装成
 *       {@link MessageSourceResolvable} 传入，由 {@code MessageSource} 跟随请求 Locale
 *       延迟渲染，不能提前解析成文案。子类中表示"资源名"的构造参数统一命名为
 *       {@code resourceNameKey}。</li>
 *   <li>调用 {@code super(messageKey, null, false, false)} 显式禁用了
 *       {@code enableSuppression} 和 {@code writableStackTrace}：业务异常
 *       属于预期内的流程分支（不是 bug），不需要抑制异常链、也不需要
 *       填充堆栈轨迹，这样可以避免在高频抛出业务异常的场景下（如参数校验）
 *       因构造堆栈信息带来不必要的性能开销。</li>
 * </ul>
 *
 * <p>子类通常按"资源 + 场景"命名，如 {@link ResourceNotFoundException}、
 * {@link ResourceDuplicateException}、{@link ResourceProtectedException}、
 * {@link ResourceInvalidException}，分别映射到不同的 HTTP 状态码，
 * 具体映射关系由全局异常处理器（{@code @RestControllerAdvice}）统一维护。
 */
@Getter
public class BusinessException extends RuntimeException {


    /**
     * 国际化消息参数，用于替换 messageKey 对应文案模板中的占位符。
     */
    private final Object[] args;

    /**
     * HTTP 响应状态码，可为 null（表示使用兜底默认值）。
     *
     * <p>子类通常有固定的状态码，由全局异常处理器中对应的专属 handler 映射，
     * 不需要设置此字段；只有当业务代码直接抛出 {@code BusinessException}
     * 且需要自定义状态码时，才通过构造函数显式传入。
     */
    private final HttpStatus status;

    public BusinessException(String messageKey, Object... args) {
        this(null, messageKey, args);
    }

    public BusinessException(HttpStatus status, String messageKey, Object... args) {
        /*
         * 业务异常属于预期中的业务失败，不需要填充 StackTrace。
         *
         * enableSuppression = false
         * writableStackTrace = false
         */
        super(messageKey, null, false, false);
        this.status = status;
        this.args = args;
    }

    /**
     * 获取响应状态码；未显式指定时返回 422 Unprocessable Entity 作为兜底。
     *
     * <p>422 表示"请求语义正确但无法执行"，适合作为通用业务异常的默认状态码：
     * 区别于 400（参数格式错误）和 500（服务端故障），明确表示"业务规则不满足"。
     */
    public HttpStatus getStatus() {
        return status != null ? status : HttpStatus.UNPROCESSABLE_ENTITY;
    }

    /**
     * 把 messages.properties 资源名词表中的 key 包装为 {@link MessageSourceResolvable}，
     * 以保证跟随请求 Locale 延迟渲染。
     *
     * <p>子类中所有表示"资源名"的构造参数（如 {@code resourceNameKey}）都应通过
     * 本方法包装后放入 {@code args}，而不是提前解析成文案。
     *
     * @param key messages.properties 资源名词表的 key（如 "resource.currency"）
     * @return 包装后的可解析对象，由 {@code MessageSource} 在渲染阶段按当前 Locale 解析
     */
    protected static MessageSourceResolvable resolvable(String key) {
        return new DefaultMessageSourceResolvable(new String[]{key});
    }
}
