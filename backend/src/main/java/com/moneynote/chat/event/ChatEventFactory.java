package com.moneynote.chat.event;

import java.util.concurrent.atomic.AtomicInteger;

import com.moneynote.chat.block.MessageBlock;
import com.moneynote.chat.event.payload.ErrorData;
import com.moneynote.chat.event.payload.FinishData;
import com.moneynote.chat.event.payload.StartData;

/**
 * SSE 事件的唯一构造点：负责填充 messageId、递增 seq、校验「事件类型 ↔ 载荷」配对。
 *
 * <p><b>每次生成 {@code new} 一个，不是 Spring 单例</b>：内部持有 per-generation 的
 * {@link AtomicInteger} seq，单例会跨请求串号。构造只绑定本次生成的 messageId，
 * 其余上下文（model / agentCode）随 start 的语义方法传入。
 *
 * <p>调用方从此不需要知道 messageId 与 seq 这两个传输层细节；新增事件类型只需在此
 * 加一个语义方法，{@code ChatEvent} 信封与编排类都不用改。
 * seq 用原子类是因为 start 在请求线程构造、finish/error 在 boundedElastic 上构造。
 */
public final class ChatEventFactory {

    private final Long messageId;
    private final AtomicInteger seq = new AtomicInteger(0);

    public ChatEventFactory(Long messageId) {
        this.messageId = messageId;
    }

    public ChatEvent start(String model, String agentCode) {
        return create(ChatEventType.START, new StartData(model, agentCode));
    }

    public ChatEvent reasoning(String delta) {
        return create(ChatEventType.REASONING, delta);
    }

    public ChatEvent content(String delta) {
        return create(ChatEventType.CONTENT, delta);
    }

    public ChatEvent finish(String finishReason) {
        return create(ChatEventType.FINISH, new FinishData(finishReason));
    }

    public ChatEvent error(String code, String message) {
        return create(ChatEventType.ERROR, new ErrorData(code, message));
    }

    /** 块事件：载荷即 block 本体，不另建数据类。 */
    public ChatEvent block(MessageBlock block) {
        return create(ChatEventType.BLOCK, block);
    }

    /** 唯一 {@code new ChatEvent}、唯一 seq 递增、唯一配对校验（原信封紧凑构造器的职责上移）。 */
    private ChatEvent create(ChatEventType type, Object data) {
        if (data != null && !type.payloadType().isInstance(data)) {
            throw new IllegalArgumentException("事件 " + type.wireName() + " 的载荷应为 "
                    + type.payloadType().getSimpleName() + "，实际为 " + data.getClass().getSimpleName());
        }
        return new ChatEvent(type, messageId, seq.incrementAndGet(), data);
    }

}
