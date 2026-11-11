package com.moneynote.chat.event;

import com.fasterxml.jackson.annotation.JsonValue;
import com.moneynote.chat.block.MessageBlock;
import com.moneynote.chat.event.payload.ErrorData;
import com.moneynote.chat.event.payload.FinishData;
import com.moneynote.chat.event.payload.StartData;

/**
 * SSE 事件类型全集（协议定义的权威位置）。
 *
 * <p>wireName 即线上格式，显式声明而不从枚举名推导——避免重命名枚举常量时静默
 * 破坏前后端协议；{@link #payloadType()} 声明该类型的 eventData 结构，由
 * {@link ChatEventFactory} 在唯一构造点做配对校验。
 *
 * <p>新增事件只需在此加常量 + 在 payload 包加一个 record + 在
 * {@link ChatEventConverter} 补一个 sealed 分支 + 在前端补一个 case，
 * 不必改动 {@code ChatEvent} 信封或任何编排类。
 */
public enum ChatEventType {

    START("start", StartData.class),
    REASONING("reasoning", String.class),
    CONTENT("content", String.class),
    FINISH("finish", FinishData.class),
    ERROR("error", ErrorData.class),
    BLOCK("block", MessageBlock.class);

    private final String wireName;
    private final Class<?> payloadType;

    ChatEventType(String wireName, Class<?> payloadType) {
        this.wireName = wireName;
        this.payloadType = payloadType;
    }

    /** 线上格式：SSE {@code event:} 字段与 JSON 内 eventType 均输出此名 */
    @JsonValue
    public String wireName() {
        return wireName;
    }

    public Class<?> payloadType() {
        return payloadType;
    }

}
